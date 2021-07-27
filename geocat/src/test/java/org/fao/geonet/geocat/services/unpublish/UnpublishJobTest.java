package org.fao.geonet.geocat.services.unpublish;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;


public class UnpublishJobTest extends AbstractCoreIntegrationTest {
    private static final Pair<Element, String> REPORT_WITH_SUCCESS = Pair.read(new Element("test-validation-success"), "test-success");
    private static final Pair<Element, String> REPORT_WITH_FAILURE = Pair.read(new Element("test-validation-error"), "test-error");

    private MetadataRepository mockMetadataRepository;
    private UnpublishNotifier mockUnpublishNotifier;
    private ServiceContext mockServiceContext;
    private SettingManager mockSettingManager;
    private IMetadataValidator mockValidator;
    private PublishRecordRepository mockPublishRecordRepository;
    private XmlSerializer mockXmlSerializer;
    private OperationAllowedRepository mockOperationAllowedRepository;
    private DataManager mockDataManager;
    private LinkRepository mockLinkRepository;
    private UrlAnalyzer mockUrlAnalyszer;

    @Test
    public void testOwnerNotifiedWhenMetadataUnpublished() throws Exception {
        initMock();
        InputStream in = UnpublishJobTest.class.getResourceAsStream("valid-metadata.iso19139.xml");
        InputStream inInvalid = UnpublishJobTest.class.getResourceAsStream("invalid-metadata.iso19139.xml");
        List<Metadata> testData = new ArrayList();
        testData.add(createMetadata(in, 1,"uuid-valid-0001", "iso19139", 101));
        testData.add(createMetadata(in, 2,"uuid-valid-0002", "iso19139", 101));
        testData.add(createMetadata(inInvalid, 3,"uuid-invalid-0003", "iso19139", 101));
        testData.add(createMetadata(inInvalid, 4,"uuid-invalid-0004", "iso19139", 101));
        testData.add(createMetadata(inInvalid, 5,"uuid-invalid-0005", "iso19139", 101));
        when(mockValidator.doValidate(any(), any(), Mockito.eq("1"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_SUCCESS);
        when(mockValidator.doValidate(any(), any(), Mockito.eq("2"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_SUCCESS);
        when(mockValidator.doValidate(any(), any(), Mockito.eq("3"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_FAILURE);
        when(mockValidator.doValidate(any(), any(), Mockito.eq("4"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_FAILURE);
        when(mockValidator.doValidate(any(), any(), Mockito.eq("5"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_FAILURE);
        when(mockMetadataRepository.findAll(any(Specification.class))).thenReturn(testData);

        UnpublishInvalidMetadataJob toTest = createToTest();

        toTest.performJob(mockServiceContext);

        ArgumentCaptor<List> impactedMetadaCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockUnpublishNotifier).notifyOwners(impactedMetadaCaptor.capture());
        Assert.assertEquals(3, impactedMetadaCaptor.getValue().size());
    }

    private void initMock() {
        mockMetadataRepository = mock(MetadataRepository.class);
        mockUnpublishNotifier = mock(UnpublishNotifier.class);
        mockServiceContext = mock(ServiceContext.class);
        mockSettingManager = mock(SettingManager.class);
        mockValidator = mock(IMetadataValidator.class);
        mockPublishRecordRepository = mock(PublishRecordRepository.class);
        mockXmlSerializer = mock(XmlSerializer.class);
        mockOperationAllowedRepository = mock(OperationAllowedRepository.class);
        mockDataManager = mock(DataManager.class);
        mockUrlAnalyszer = mock(UrlAnalyzer.class);
        mockLinkRepository = mock(LinkRepository.class);
        when(mockServiceContext.getBean(MetadataRepository.class)).thenReturn(mockMetadataRepository);
        when(mockSettingManager.getValueAsInt("system/metadata/publish_tracking_duration", 100)).thenReturn(100);
        when(mockLinkRepository.findAll()).thenReturn(Collections.emptyList());
    }

    private UnpublishInvalidMetadataJob createToTest() {
        UnpublishInvalidMetadataJob toTest = new UnpublishInvalidMetadataJob() {
            protected void indexMetadataWithNonEvaluatedOrIncoherentValidationStatus(ServiceContext serviceContext, DataManager dataManager) {

            }
            protected boolean isPublished(int id) {
                return true;
            }
            protected Pair<String, String> failureReason(Element report) {
                if (report.getName() == "test-validation-error") {
                    return Pair.read("error", "error");
                } else {
                    return Pair.read("", "");
                }
            }
            protected boolean hasValidationRecord(Integer id) {
                return false;
            }
        };
        toTest.setUnpublishNotifier(mockUnpublishNotifier);
        toTest.settingManager = mockSettingManager;
        toTest.metadataValidator = mockValidator;
        toTest.publishRecordRepository = mockPublishRecordRepository;
        toTest.xmlSerializer = mockXmlSerializer;
        toTest.operationAllowedRepository = mockOperationAllowedRepository;
        toTest.dataManager = mockDataManager;
        toTest.linkRepository = mockLinkRepository;
        toTest.urlAnalyzer = mockUrlAnalyszer;
        toTest.appContext = _applicationContext;
        return toTest;
    }

    private Metadata createMetadata(InputStream in, int id, String uuid, String schemaId, int owner) throws IOException {
        Metadata md = new Metadata();
        md.setData(IOUtils.toString(in))
                .setId(id)
                .setUuid(uuid);
        md.getDataInfo()
                .setSchemaId(schemaId);
        md.getSourceInfo()
                .setOwner(owner);
        return md;
    }
}
