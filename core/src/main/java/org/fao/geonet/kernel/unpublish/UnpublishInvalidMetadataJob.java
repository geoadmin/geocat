/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.kernel.unpublish;

import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
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
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.fao.geonet.repository.geocat.specification.PublishRecordSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.filter.Filter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.isPublic;
import static org.springframework.data.jpa.domain.Specification.where;

public class UnpublishInvalidMetadataJob extends QuartzJobBean {
    public static final String AUTOMATED_ENTITY = "Automated";

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.GEONETWORK + ".unpublish");
    private static final Logger LOGGER_DATA_MAN = LoggerFactory.getLogger(Geonet.DATA_MANAGER);
    private static final Logger LOGGER_GEONET = LoggerFactory.getLogger(Geonet.GEONETWORK);
    public static String VAL_STATUS_NOT_EVALUATED = "-1";
    public static String VAL_STATUS_VALID = "1";

    public void setUnpublishNotifier(UnpublishNotifier unpublishNotifier) {
        this.unpublishNotifier = unpublishNotifier;
    }

    private UnpublishNotifier unpublishNotifier;
    private List<Metadata> unpublishedRecords = new ArrayList<>();

    @Autowired
    protected XmlSerializer xmlSerializer;
    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    protected OperationAllowedRepository operationAllowedRepository;
    @Autowired
    private MetadataValidationRepository metadataValidationRepository;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected PublishRecordRepository publishRecordRepository;
    @Autowired
    public EsSearchManager esSearchManager;

    /*
    @Autowired
    private SearchManager searchManager;
    */

    @Autowired
    protected SettingManager settingManager;
    @Autowired
    protected IMetadataValidator metadataValidator;
    @Autowired
    protected IMetadataManager metadataManager;
    @Autowired
    protected LinkRepository linkRepository;
    @Autowired
    protected UrlAnalyzer urlAnalyzer;
    @Autowired
    protected ApplicationContext appContext;
    @Autowired
    IMetadataIndexer metadataIndexer;

    @PersistenceContext
    protected EntityManager entityManager;

    private AtomicBoolean running = new AtomicBoolean(false);

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

    public static final Filter ErrorFinder = new Filter() {
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


    protected Pair<String, String> failureReason(Element report) {
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

    private void processXsdError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = "Xsd Error";
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

    private void processSchematronError(Element report, StringBuilder rules, StringBuilder failures) {
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
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        ApplicationContextHolder.set(this.context);
        final UserRepository userRepository = context.getBean(UserRepository.class);
        unpublishNotifier = new UnpublishNotifier(userRepository, settingManager);

        int id = 1;

        try {
            final List<User> allByProfile = userRepository.findAllByProfile(Profile.Administrator);
            if (!allByProfile.isEmpty()) {
                id = allByProfile.get(0).getId();
            }
        } catch (Throwable e) {
            LOGGER_DATA_MAN.error("Error during unpublish", e);
        }

        ServiceContext serviceContext = serviceManager.createServiceContext("unpublishMetadata", context);
        serviceContext.setAsThreadLocal();

        final UserSession userSession = new UserSession();
        User user = new User();
        user.setId(id);
        user.setProfile(Profile.Administrator);
        user.setUsername("admin");

        userSession.loginAs(user);
        serviceContext.setUserSession(userSession);
        try {
            performJob(serviceContext);
        } catch (Exception e) {
            LOGGER_GEONET.error("Error running {}", UnpublishInvalidMetadataJob.class.getSimpleName(), e);
        }

    }

    protected void performJob(ServiceContext serviceContext) throws Exception {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Unpublish Job is already running");
        }
        try {
            long startTime = System.currentTimeMillis();
            LOGGER.info("Starting Unpublish Invalid Metadata Job");

            serviceContext.setAsThreadLocal();
            Integer keepDuration = settingManager.getValueAsInt("system/metadata/publish_tracking_duration", 100);

            // clean up expired changes
            publishRecordRepository.deleteAll(PublishRecordSpecs.daysOldOrOlder(keepDuration));

            List<Metadata> metadataToTest = lookUpMetadataIds(serviceContext.getBean(MetadataRepository.class));
            unpublishedRecords.clear();
            for (Metadata metadataRecord : metadataToTest) {
                try {
                    tryToValidateRecord(serviceContext, metadataRecord);
                } catch (Exception e) {
                    String error = Xml.getString(JeevesException.toElement(e));
                    LOGGER.error("Error during Validation/Unpublish process of metadata {}.  Exception: {}", metadataRecord.getId(), error);
                }
                dataManager.flush();
            }
            unpublishNotifier.notifyOwners(unpublishedRecords);

            LOGGER.info("Finishing Unpublish Invalid Metadata Job.  Job took:  {} sec", MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));

            indexMetadataWithNonEvaluatedOrIncoherentValidationStatus(serviceContext, dataManager);

            checkUrl();

        } finally {
            running.set(false);
        }
    }


    protected void indexMetadataWithNonEvaluatedOrIncoherentValidationStatus(ServiceContext serviceContext, DataManager dataManager) throws Exception {
        LOGGER.info("Start indexing metadata with non evaluated or incoherent validation status.");
        long startTime = System.currentTimeMillis();

        SearchResponse response = esSearchManager.query("+isTemplate: n +isHarvested: false", null,  Stream.of("valid", "id")
            .collect(Collectors.toCollection(HashSet::new)), 0, 15000);

        Set<String> toIndex = Sets.newHashSet();
        Iterator<SearchHit> it = response.getHits().iterator();
        while(it.hasNext()) {
            SearchHit hit = it.next();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String docId = sourceAsMap.get("id").toString();
            if (sourceAsMap.get("valid") != null) {
                String indexedValidationStatus = sourceAsMap.get("valid").toString();
                if (indexedValidationStatus.equalsIgnoreCase(VAL_STATUS_NOT_EVALUATED)) {
                    toIndex.add(docId);
                } else {
                    boolean isIndexedValid = indexedValidationStatus.equalsIgnoreCase(VAL_STATUS_VALID);
                    boolean persistedValid = metadataManager.isValid(Integer.parseInt(docId));
                    boolean incoherentState = isIndexedValid != persistedValid;
                    if (incoherentState) {
                        toIndex.add(docId);
                    }
                }
            } else {
                toIndex.add(docId);
            }
        }

        for (String mdId : toIndex) {
            metadataIndexer.indexMetadata(mdId, false, IndexingMode.full);
        }
        long timeSec = MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        LOGGER.info("Finishing with non evaluated or incoherent validation status. It took: {} sec", timeSec);
    }

    private void tryToValidateRecord(ServiceContext context, Metadata metadataRecord) throws Exception {
        boolean published = isPublished(metadataRecord.getId());
        boolean hasValidationRecord = hasValidationRecord(metadataRecord.getId());
        if (!published && !hasValidationRecord) {
            return;
        }

        Element md = xmlSerializer.select(context, String.valueOf(metadataRecord.getId()));
        String schema = metadataRecord.getDataInfo().getSchemaId();

        String id = "" + metadataRecord.getId();
        Element report = metadataValidator.doValidate(context.getUserSession(), schema, id, md, "eng", false).one();
        Pair<String, String> failureReport = failureReason(report);
        String failureRule = failureReport.one();
        String failureReasons = failureReport.two();

        analyzeUrl(metadataRecord, md);

        if (failureRule.isEmpty() || !published) {
            return;
        }

        PublishRecord todayRecord = new PublishRecord();
        todayRecord.setChangedate(new Date());
        todayRecord.setChangetime(new Date());
        todayRecord.setFailurereasons(failureReasons);
        todayRecord.setFailurerule(failureRule);
        todayRecord.setUuid(metadataRecord.getUuid());
        todayRecord.setEntity(AUTOMATED_ENTITY);
        todayRecord.setPublished(false);
        todayRecord.setGroupOwner(metadataRecord.getSourceInfo().getGroupOwner());
        todayRecord.setSource(metadataRecord.getSourceInfo().getSourceId());
        todayRecord.setValidated(PublishRecord.Validity.fromBoolean(false));
        publishRecordRepository.save(todayRecord);

        Specification<OperationAllowed> publicOps = where(isPublic(ReservedOperation.view))
                .or(isPublic(ReservedOperation.download))
                .or(isPublic(ReservedOperation.editing))
                .or(isPublic(ReservedOperation.featured))
                .or(isPublic(ReservedOperation.dynamic));
        operationAllowedRepository.deleteAll(where(hasMetadataId(metadataRecord.getId())).and(publicOps));

        unpublishedRecords.add(metadataRecord);
    }



    protected boolean isPublished(int id) throws SQLException {
        Specification<OperationAllowed> idAndPublishedSpec =
                where(isPublic(ReservedOperation.view)).
                        and(OperationAllowedSpecs.hasMetadataId("" + id));
        return operationAllowedRepository.count(idAndPublishedSpec) > 0;
    }

    protected boolean hasValidationRecord(Integer id) {
        List<MetadataValidation> validationInfo = metadataValidationRepository.findAllById_MetadataId(id);
        if (validationInfo == null || validationInfo.size() == 0) {
            return false;
        }
        return true;
    }

    private void analyzeUrl(Metadata metadataRecord, Element md) throws org.jdom.JDOMException {
        TransactionManager.runInTransaction("analyze url",appContext,
                TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                        urlAnalyzer.processMetadata(md, metadataRecord);
                        return null;
                    }
                });
    }

    private void checkUrl() {
        linkRepository.findAll().stream().forEach(link -> {
            TransactionManager.runInTransaction("check url", appContext,
                TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, new TransactionTask<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                        urlAnalyzer.testLink(link);
                        return null;
                    }
                });
        });


    }

    /**
     * Creates a list of metadata needed to be schematron checked, i.e.:
     * <p>
     * - not harvested
     * - of type Metadata (no template nor subtemplate)
     * - of schema ISO19139.che
     *
     * @param repo the MetadataRepository JPA
     * @return a list of metadata objects to check
     * @throws SQLException
     */
    private List<Metadata> lookUpMetadataIds(MetadataRepository repo) {
        final Specification<Metadata> notHarvested = (Specification<Metadata>) MetadataSpecs.isHarvested(false);
        Specification<Metadata> isMetadata = (Specification<Metadata>) MetadataSpecs.isType(MetadataType.METADATA);
        Specification<Metadata> isCHEMetadata = (Specification<Metadata>) MetadataSpecs.hasSchemaId("iso19139.che");
        return repo.findAll(where(notHarvested).and(isMetadata).and(isCHEMetadata));
    }

    /**
     * Gets a list of published states for the MDs, between 2 dates given as argument.
     *
     * @param context     a ServiceContext object, used to get a hook onto the JPA repositories
     * @param startOffset
     * @param endOffset
     * @return the list of published states (see the publish_tracking table in db)
     * @throws Exception
     */
    static List<PublishRecord> values(ServiceContext context, int startOffset, int endOffset) {
        final Specification<PublishRecord> daysOldOrNewer = PublishRecordSpecs.daysOldOrNewer(startOffset);
        final Specification<PublishRecord> daysOldOrOlder = PublishRecordSpecs.daysOldOrOlder(endOffset);

        return context.getBean(PublishRecordRepository.class).findAll(where(daysOldOrNewer).and(daysOldOrOlder));
    }
}
