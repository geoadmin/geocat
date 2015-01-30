package org.fao.geonet.geocat.kernel;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.fao.geonet.repository.geocat.specification.PublishRecordSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.data.jpa.domain.Specifications.where;

public class UnpublishInvalidMetadataJob extends QuartzJobBean implements Service {

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private ServiceManager serviceManager;
    static final String AUTOMATED_ENTITY = "Automated";

    AtomicBoolean running = new AtomicBoolean(false);

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
        try {
            performJob(context);
            return new Element("status").setText("true");
        } catch (Throwable e) {
            return new Element("status").setText("false");
        }
    }

    // --------------------------------------------------------------------------------

    private void performJob(ServiceContext serviceContext) throws Exception {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Unpublish Job is already running");
        }
        try {
            Integer keepDuration = serviceContext.getBean(SettingManager.class).getValueAsInt("system/publish_tracking_duration");
            if (keepDuration == null) {
                keepDuration = 100;
            }


            List<Metadata> metadataToTest;

            // clean up expired changes
            final PublishRecordRepository publishRepository = serviceContext.getBean(PublishRecordRepository.class);
            publishRepository.deleteAll(PublishRecordSpecs.daysOldOrNewer(Math.max(2, keepDuration)));

            metadataToTest = lookUpMetadataIds(serviceContext.getBean(MetadataRepository.class));

            DataManager dataManager = serviceContext.getBean(DataManager.class);
            for (Metadata metadataRecord : metadataToTest) {
                final String id = "" + metadataRecord.getId();
                try {
                     if(checkIfNeedsUnpublishingAndSavePublishedRecord(serviceContext, metadataRecord, dataManager)) {
                         dataManager.indexMetadata(id, false, false, true, true);
                     }
                } catch (Exception e) {
                    String error = Xml.getString(JeevesException.toElement(e));
                    Log.error(Geonet.INDEX_ENGINE, "Error during Validation/Unpublish process of metadata " + id + ".  Exception: "
                                                   + error);
                }
            }
        } finally {
            running.set(false);
        }
    }

    private boolean checkIfNeedsUnpublishingAndSavePublishedRecord(ServiceContext context, Metadata metadataRecord,
                                                                   DataManager dataManager) throws Exception {
        String id = "" + metadataRecord.getId();
        SchemaManager schemaManager = context.getBean(SchemaManager.class);
        Element md = metadataRecord.getXmlData(false);
        String schema = schemaManager.autodetectSchema(md);
        PublishRecord todayRecord = null;
        boolean published = isPublished(id, context);

        if (published) {
            Element report = dataManager.doValidate(null, schema, id, md, "eng", false).one();

            Pair<String, String> failureReport = failureReason(context, report);
            String failureRule = failureReport.one();
            String failureReasons = failureReport.two();
            if (!failureRule.isEmpty()) {
                boolean validated = false;
                String entity = AUTOMATED_ENTITY;
                published = false;
                todayRecord = new PublishRecord();
                todayRecord.setChangedate(new Date());
                todayRecord.setChangetime(new Date());
                todayRecord.setFailurereasons(failureReasons);
                todayRecord.setFailurerule(failureRule);
                todayRecord.setUuid(metadataRecord.getUuid());
                todayRecord.setEntity(entity);
                todayRecord.setPublished(published);
                todayRecord.setValidated(PublishRecord.Validity.fromBoolean(validated));
                context.getBean(PublishRecordRepository.class).save(todayRecord);
                return true;
            }
        }

        return false;
    }

    public static boolean isPublished(String id, ServiceContext context) throws SQLException {
        final OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);

        final Specifications<OperationAllowed> idAndPublishedSpec = where(OperationAllowedSpecs.isPublic(ReservedOperation.view)).and
                (OperationAllowedSpecs.hasMetadataId(id));
        return allowedRepository.count(idAndPublishedSpec) > 0;
    }

    private Pair<String, String> failureReason(ServiceContext context, Element report) {

        @SuppressWarnings("unchecked")
        Iterator<Element> reports = report.getDescendants(new ReportFinder());

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
        @SuppressWarnings("unchecked")
        Iterator<Element> errors = report.getDescendants(new ErrorFinder());
        if (errors.hasNext()) {
            if (rules.length() > 0)
                rules.append("\n");
            rules.append(reportType);
        }

        while (errors.hasNext()) {
            if (failures.length() > 0)
                failures.append('\n');

            Element error = errors.next();
            failures.append(error.getChildText("message", Edit.NAMESPACE));
            failures.append(" xpath[");
            failures.append(error.getChildText("xpath", Edit.NAMESPACE));
            failures.append("]");
        }
    }

    private void processSchematronError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = report.getAttributeValue("rule", Edit.NAMESPACE);
        reportType = reportType == null ? "No name for rule" : reportType;
        StringBuilder failure = new StringBuilder();

        boolean isMandatory = Boolean.parseBoolean(report.getAttributeValue("required", Edit.NAMESPACE));

        if (isMandatory) {
            @SuppressWarnings("unchecked")
            Iterator<Element> errors = report.getDescendants(new ErrorFinder());

            while (errors.hasNext()) {
                if (failure.length() > 0 || failures.length() > 0)
                    failure.append('\n');

                Element error = errors.next();

                Element text = error.getChild("text", Namespace.getNamespace("http://purl.oclc.org/dsdl/svrl"));
                if (text == null || !(reportType.equals("schematron-rules-iso-che") && text.getChild("alert.M104") != null)) {
                    failure.append(error.getAttributeValue("test"));
                    failure.append(" xpath[");
                    failure.append(error.getAttributeValue("location"));
                    failure.append("]");
                }
            }

            if (failure.length() > 0) {
                if (rules.length() > 0)
                    rules.append("\n");
                rules.append(reportType);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private List<Metadata> lookUpMetadataIds(MetadataRepository repo) throws SQLException {
        final Specification<Metadata> notHarvested = MetadataSpecs.isHarvested(false);
        Specification<Metadata> isMetadata = MetadataSpecs.isType(MetadataType.METADATA);
        return repo.findAll(where(notHarvested).and(isMetadata));
    }

    @SuppressWarnings("unchecked")
    static List<PublishRecord> values(ServiceContext context, int startOffset, int endOffset) throws Exception {
        final Specification<PublishRecord> daysOldOrNewer = PublishRecordSpecs.daysOldOrNewer(startOffset);
        final Specification<PublishRecord> daysOldOrOlder = PublishRecordSpecs.daysOldOrOlder(endOffset);
        final List<PublishRecord> records = context.getBean(PublishRecordRepository.class).findAll(where(daysOldOrNewer).and(daysOldOrOlder));

        return records;
    }


    static class ErrorFinder implements Filter {
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
    }
    static class ReportFinder implements Filter {
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
    }
}
