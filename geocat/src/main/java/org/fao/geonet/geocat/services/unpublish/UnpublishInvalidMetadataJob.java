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
package org.fao.geonet.geocat.services.unpublish;

import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.geocat.services.aap.AapMetadataReport;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.*;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.isPublic;

@Component
public class UnpublishInvalidMetadataJob extends QuartzJobBean {
    public static final String AUTOMATED_ENTITY = "Automated";

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.GEONETWORK + ".unpublish");
    private static final Logger LOGGER_DATA_MAN = LoggerFactory.getLogger(Geonet.DATA_MANAGER);
    private static final Logger LOGGER_GEONET = LoggerFactory.getLogger(Geonet.GEONETWORK);

    @Autowired
    private UnpublishNotifier unpublishNotifier;

    public void setUnpublishNotifier(UnpublishNotifier unpublishNotifier) {
        this.unpublishNotifier = unpublishNotifier;
    }

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
    private EsSearchManager searchManager;
    @Autowired
    protected UserRepository userRepository;
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

    public void performJob(ServiceContext serviceContext) throws Exception {
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


    public static String VAL_STATUS_NOT_EVALUATED = "-1";
    public static String VAL_STATUS_INVALID = "0";
    public static String VAL_STATUS_VALID = "1";

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    private int size = 10000;

    protected void indexMetadataWithNonEvaluatedOrIncoherentValidationStatus(
        ServiceContext serviceContext, DataManager dataManager) throws Exception {
        LOGGER.info("Search metadata with non evaluated or incoherent validation status.");
        long startTime = System.currentTimeMillis();
        Set<String> source = new HashSet<String>();
        source.add(Geonet.IndexFieldNames.UUID);
        source.add(Geonet.IndexFieldNames.ID);
        source.add("valid");

        SearchResponse response = searchManager.query(
//            "+uuid:\"b353f39c-6349-49e0-97d0-c522fc2fbf0f\" +isHarvested:false +isTemplate:n +documentStandard:iso19139.che",
            "+isHarvested:false +isTemplate:n +documentStandard:iso19139.che",
            null, source, 0, size);
        Set<String> recordsWithIncoherentValidationStates =
            Arrays.stream(response.getHits().getHits())
            .filter(h -> {
                String valid = (String) h.getSourceAsMap().get("valid");
                if (VAL_STATUS_NOT_EVALUATED.equals(valid)) {
                    return true;
                } else if (VAL_STATUS_VALID.equals(valid)) {
                    String id = (String) h.getSourceAsMap().get(Geonet.IndexFieldNames.ID);
                    boolean persistedValid = metadataManager.isValid(Integer.parseInt(id));
                    return !VAL_STATUS_VALID.equals(persistedValid);
                }
                return false;
            })
            .map(h -> (String) h.getSourceAsMap().get(Geonet.IndexFieldNames.ID))
            .collect(Collectors.toSet());

        LOGGER.info("Start indexing {} records.", recordsWithIncoherentValidationStates.size());

        for (String mdId : recordsWithIncoherentValidationStates) {
            LOGGER.info("  Indexing #{}", mdId);
            dataManager.indexMetadata(mdId, false);
        }
        long timeSec = MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        LOGGER.info("Finishing with non evaluated or incoherent validation status. It took: {} sec", timeSec);
    }

    private void tryToValidateRecord(ServiceContext context, Metadata metadataRecord) throws Exception {
        boolean published = isPublished(metadataRecord.getId());
        boolean hasValidationRecord = hasValidationRecord(metadataRecord.getId());
        if (!published && !hasValidationRecord) {
            LOGGER.debug("  {} / Skip unpublished record with no validation status.", metadataRecord.getUuid());
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

        LOGGER.debug("  {} / Validation error {}, published {}",
            metadataRecord.getUuid(),
            failureRule.length(),
            published);
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

        Specification<OperationAllowed> publicOps = Specification
            .where(isPublic(ReservedOperation.view))
            .or(isPublic(ReservedOperation.download))
            .or(isPublic(ReservedOperation.editing))
            .or(isPublic(ReservedOperation.featured))
            .or(isPublic(ReservedOperation.dynamic));
        operationAllowedRepository.deleteAll(Specification
            .where(hasMetadataId(metadataRecord.getId()))
            .and(publicOps));

        unpublishedRecords.add(metadataRecord);
    }


    protected boolean isPublished(int id) throws SQLException {
        Specification<OperationAllowed> idAndPublishedSpec =
            Specification.where(isPublic(ReservedOperation.view)).
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
        TransactionManager.runInTransaction("analyze url", appContext,
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
    private List<Metadata> lookUpMetadataIds(MetadataRepository repo) throws SQLException {
        final Specification<Metadata> notHarvested = (Specification<Metadata>) MetadataSpecs.isHarvested(false);
        Specification<Metadata> isMetadata = (Specification<Metadata>) MetadataSpecs.isType(MetadataType.METADATA);
        Specification<Metadata> isCHEMetadata = (Specification<Metadata>) MetadataSpecs.hasSchemaId("iso19139.che");
//        Specification<Metadata> isRecord = (Specification<Metadata>) MetadataSpecs.hasMetadataUuid("b353f39c-6349-49e0-97d0-c522fc2fbf0f");
//        return repo.findAll(Specification.where(notHarvested).and(isRecord));
        return repo.findAll(Specification.where(notHarvested).and(isMetadata).and(isCHEMetadata));
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
    static List<PublishRecord> values(ServiceContext context, int startOffset, int endOffset) throws Exception {
        final Specification<PublishRecord> daysOldOrNewer = PublishRecordSpecs.daysOldOrNewer(startOffset);
        final Specification<PublishRecord> daysOldOrOlder = PublishRecordSpecs.daysOldOrOlder(endOffset);

        return context.getBean(PublishRecordRepository.class)
            .findAll(Specification.where(daysOldOrNewer).and(daysOldOrOlder));
    }
}
