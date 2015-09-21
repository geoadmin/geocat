package org.fao.geonet.geocat.kernel;

import com.google.common.collect.Sets;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.search.DuplicateDocFilter;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.fao.geonet.repository.geocat.specification.PublishRecordSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.schema.iso19139che.ISO19139cheSchemaPlugin;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.Filter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.isPublic;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.springframework.data.jpa.domain.Specifications.where;

public class UnpublishInvalidMetadataJob extends QuartzJobBean implements Service {
    public static final String UNPUBLISH_LOG = Geonet.GEONETWORK + ".unpublish";
    public static final Filter ReportFinder = new Filter() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("report") || name.equals("xsderrors")) {
                    return true;
                }
            }
            return false;
        }
    };
    public static final Filter ErrorFinder  = new Filter() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("error")) {
                    return true;
                } else if (name.equals("failed-assert")) {
                    return true;
                }
            }
            return false;
        }
    };
    @Autowired
    XmlSerializer xmlSerializer;
    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private OperationAllowedRepository operationAllowedRepository;
    static final String AUTOMATED_ENTITY = "Automated";

    AtomicBoolean running = new AtomicBoolean(false);

    public static Pair<String, String> failureReason(ServiceContext context, Element report) {

        @SuppressWarnings("unchecked")
        Iterator<Element> reports = report.getDescendants(ReportFinder);

        StringBuilder rules = new StringBuilder();
        StringBuilder failures = new StringBuilder();
        while (reports.hasNext()) {
            report = reports.next();
            if (report.getName().equals("xsderrors")) {
                processXsdError(report, rules, failures);
            } else {
                processSchematronError(report, rules, failures);
            }
        }

        return Pair.read(rules.toString(), failures.toString());
    }

    private static void processXsdError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = "Xsd Error";
        @SuppressWarnings("unchecked")
        Iterator<Element> errors = report.getDescendants(ErrorFinder);
        if (errors.hasNext()) {
            rules.append("<div class=\"rule\">").append(reportType).append("</div>");

            while (errors.hasNext()) {
                failures.append("<div class=\"failure\">");
                Element error = errors.next();
                failures.append("</div><div class=\"xpath\">");
                failures.append(" XPATH of failure:");
                failures.append(error.getChildText("xpath", Edit.NAMESPACE));
                failures.append("</div><h4>Reason</h4><div class=\"reason\">");
                failures.append(error.getChildText("message", Edit.NAMESPACE));
                failures.append("</div>");
                failures.append("</div>");
            }
        }
    }

    private static void processSchematronError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = report.getAttributeValue("rule", Edit.NAMESPACE);
        reportType = reportType == null ? "No name for rule" : reportType;

        boolean isMandatory = SchematronRequirement.REQUIRED.name().equals(report.getAttributeValue("required", Edit.NAMESPACE));

        if (isMandatory) {
            @SuppressWarnings("unchecked")
            Iterator<Element> errors = report.getDescendants(ErrorFinder);

            if (errors.hasNext()) {
                rules.append("<div class=\"rule\">").append(reportType).append("</div>");

                while (errors.hasNext()) {
                    failures.append("<div class=\"failure\">\n");

                    Element error = errors.next();

                    Element text = error.getChild("text", Geonet.Namespaces.SVRL);
                    if (text != null) {
                        failures.append("  <div class=\"test\">Schematron Test: ");
                        failures.append(error.getAttributeValue("test"));
                        failures.append("  </div>\n  <div class=\"xpath\">");
                        failures.append("XPATH of failure: ");
                        failures.append(error.getAttributeValue("location"));
                        failures.append("  </div>\n<h4>Reason</h4><div class=\"reason\">");
                        List children = text.getContent();

                        for (Object child : children) {
                            if (child instanceof Element) {
                                failures.append(Xml.getString((Element) child));
                            } else if (child instanceof Text) {
                                failures.append(((Text) child).getText());
                            }
                        }
                        failures.append("  </div>\n");
                    } else {
                        failures.append("unknown reason");
                    }
                    failures.append("</div>\n");
                }
            }
        }
    }

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }


    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        ApplicationContextHolder.set(this.context);
        final UserRepository userRepository = context.getBean(UserRepository.class);

        int id = 1;

        try {
            final List<User> allByProfile = userRepository.findAllByProfile(Profile.Administrator);
            if (!allByProfile.isEmpty()) {
                id = allByProfile.get(0).getId();
            }
        } catch (Throwable e) {
            Log.error(Geonet.DATA_MANAGER, "Error during unpublish", e);
        }

        ServiceContext serviceContext = serviceManager.createServiceContext("unpublishMetadata", context);
        serviceContext.setAsThreadLocal();

        final UserSession userSession = new UserSession();
        User user = new User();
        user.setId(id);
        user.setProfile(Profile.Administrator);
        user.setUsername("admin");

        userSession.loginAs(user);
        try {
            performJob(serviceContext);
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK, "Error running " + UnpublishInvalidMetadataJob.class.getSimpleName(), e);
        }

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        final Trigger trigger = newTrigger().forJob("unpublishInvalidMetadata", "geocatBackgroundTasks").startNow().build();
        context.getApplicationContext().getBean("geocatBackgroundJobScheduler", Scheduler.class).scheduleJob(trigger);
        return new Element("ok");
    }

    // --------------------------------------------------------------------------------

    private void performJob(ServiceContext serviceContext) throws Exception {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Unpublish Job is already running");
        }
        try {
            long startTime = System.currentTimeMillis();
            Log.info(UNPUBLISH_LOG, "Starting Unpublish Invalid Metadata Job");
            Integer keepDuration = serviceContext.getBean(SettingManager.class).getValueAsInt("system/metadata/publish_tracking_duration");
            if (keepDuration == null) {
                keepDuration = 100;
            }


            List<Metadata> metadataToTest;

            // clean up expired changes
            final PublishRecordRepository publishRepository = serviceContext.getBean(PublishRecordRepository.class);
            publishRepository.deleteAll(PublishRecordSpecs.daysOldOrOlder(keepDuration));

            metadataToTest = lookUpMetadataIds(serviceContext.getBean(MetadataRepository.class));

            DataManager dataManager = serviceContext.getBean(DataManager.class);
            for (Metadata metadataRecord : metadataToTest) {
                ApplicationContextHolder.set(serviceContext.getApplicationContext());
                serviceContext.setAsThreadLocal();
                final String id = "" + metadataRecord.getId();
                try {
                     if(checkIfNeedsUnpublishingAndSavePublishedRecord(serviceContext, metadataRecord, dataManager)) {
                         dataManager.indexMetadata(id, false, false, true, false);
                     }
                } catch (Exception e) {
                    String error = Xml.getString(JeevesException.toElement(e));
                    Log.error(UNPUBLISH_LOG, "Error during Validation/Unpublish process of metadata " + id + ".  Exception: " + error);
                }

                serviceContext.getBean(DataManager.class).flush();
            }

            long timeSec = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
            Log.info(UNPUBLISH_LOG, "Finishing Unpublish Invalid Metadata Job.  Job took:  " + timeSec + " sec");

            indexNonValidatedMetadata(serviceContext, dataManager);
        } finally {
            running.set(false);
        }
    }

    private void indexNonValidatedMetadata(ServiceContext serviceContext, DataManager dataManager) throws Exception {
        Log.info(UNPUBLISH_LOG, "Start Unpublish Invalid Metadata Job.");
        long startTime = System.currentTimeMillis();
        SearchManager searchManager = serviceContext.getBean(SearchManager.class);
        Set<String> ids = Sets.newHashSet();
        try (IndexAndTaxonomy iat = searchManager.getNewIndexReader(null)) {
            IndexSearcher searcher = new IndexSearcher(iat.indexReader);
            BooleanQuery query = new BooleanQuery();
            query.add(new BooleanClause(new TermQuery(new Term("_valid", "-1")), BooleanClause.Occur.MUST));
            query.add(new BooleanClause(new TermQuery(new Term(Geonet.IndexFieldNames.IS_HARVESTED, "n")), BooleanClause.Occur.MUST));
            query.add(new BooleanClause(new TermQuery(new Term(Geonet.IndexFieldNames.IS_TEMPLATE, "n")), BooleanClause.Occur.MUST));
            query.add(new BooleanClause(new TermQuery(new Term(Geonet.IndexFieldNames.SCHEMA, "iso19139.che")), BooleanClause.Occur.MUST));
            TopDocs topDocs = searcher.search(query, new DuplicateDocFilter(query), Integer.MAX_VALUE);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                ids.add(searcher.doc(scoreDoc.doc).get(Geonet.IndexFieldNames.ID));
            }
        }

        for (String mdId : ids) {
            dataManager.indexMetadata(mdId, false, true, false, true);
        }
        long timeSec = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        Log.info(UNPUBLISH_LOG, "Finishing Indexing metadata that have not been validated in for index.  "
                                + "Job took:  " + timeSec + " sec");
    }

    private boolean checkIfNeedsUnpublishingAndSavePublishedRecord(ServiceContext context, Metadata metadataRecord,
                                                                   DataManager dataManager) throws Exception {
        String id = "" + metadataRecord.getId();
        Element md   = xmlSerializer.selectNoXLinkResolver(String.valueOf(metadataRecord.getId()), true);
        String schema = metadataRecord.getDataInfo().getSchemaId();
        PublishRecord todayRecord;
        boolean published = isPublished(id, context);

        if (published) {
            Element report = dataManager.doValidate(context, schema, id, md, "eng", false, true).one();

            Pair<String, String> failureReport = failureReason(context, report);
            String failureRule = failureReport.one();
            String failureReasons = failureReport.two();
            if (!failureRule.isEmpty()) {
                todayRecord = new PublishRecord();
                todayRecord.setChangedate(new Date());
                todayRecord.setChangetime(new Date());
                todayRecord.setFailurereasons(failureReasons);
                todayRecord.setFailurerule(failureRule);
                todayRecord.setUuid(metadataRecord.getUuid());
                todayRecord.setEntity(AUTOMATED_ENTITY);
                todayRecord.setPublished(false);
                todayRecord.setValidated(PublishRecord.Validity.fromBoolean(false));
                context.getBean(PublishRecordRepository.class).save(todayRecord);

                final Specifications<OperationAllowed> publicOps = Specifications.where(isPublic(ReservedOperation.view)).
                        or(isPublic(ReservedOperation.download)).
                        or(isPublic(ReservedOperation.editing)).
                        or(isPublic(ReservedOperation.featured)).
                        or(isPublic(ReservedOperation.dynamic));
                operationAllowedRepository.deleteAll(Specifications.where(hasMetadataId(metadataRecord.getId())).and(publicOps));
                return true;
            }
        }

        return false;
    }

    public static boolean isPublished(String id, ServiceContext context) throws SQLException {
        final OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);

        final Specifications<OperationAllowed> idAndPublishedSpec = where(isPublic(ReservedOperation.view)).and
                (OperationAllowedSpecs.hasMetadataId(id));
        return allowedRepository.count(idAndPublishedSpec) > 0;
    }

    @SuppressWarnings("unchecked")
    private List<Metadata> lookUpMetadataIds(MetadataRepository repo) throws SQLException {
        final Specification<Metadata> notHarvested = MetadataSpecs.isHarvested(false);
        Specification<Metadata> isMetadata = MetadataSpecs.isType(MetadataType.METADATA);
        Specification<Metadata> isCHEMetadata = MetadataSpecs.hasSchemaId(ISO19139cheSchemaPlugin.IDENTIFIER);
        return repo.findAll(where(notHarvested).and(isMetadata).and(isCHEMetadata));
    }

    @SuppressWarnings("unchecked")
    static List<PublishRecord> values(ServiceContext context, int startOffset, int endOffset) throws Exception {
        final Specification<PublishRecord> daysOldOrNewer = PublishRecordSpecs.daysOldOrNewer(startOffset);
        final Specification<PublishRecord> daysOldOrOlder = PublishRecordSpecs.daysOldOrOlder(endOffset);

        return context.getBean(PublishRecordRepository.class).findAll(where(daysOldOrNewer).and(daysOldOrOlder));
    }
}
