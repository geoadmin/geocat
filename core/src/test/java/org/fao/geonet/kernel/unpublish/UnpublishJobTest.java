package org.fao.geonet.kernel.unpublish;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.geocat.PublishRecordRepository;

import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specifications;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UnpublishJobTest {
    private static final Pair<Element, String> REPORT_WITH_SUCCESS = Pair.read(new Element("test-validation-success"), "test-success");
    private static final Pair<Element, String> REPORT_WITH_FAILURE = Pair.read(new Element("test-validation-error"), "test-error");

    private MetadataRepository mockMetadataRepository;
    private MailSender mockMailSender;
    private ServiceContext mockServiceContext;
    private SettingManager mockSettingManager;
    private DataManager mockDataManager;
    private PublishRecordRepository mockPublishRecordRepository;
    private XmlSerializer mockXmlSerializer;
    private OperationAllowedRepository mockOperationAllowedRepository;

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
        when(mockDataManager.doValidate(any(), any(), Mockito.eq("1"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_SUCCESS);
        when(mockDataManager.doValidate(any(), any(), Mockito.eq("2"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_SUCCESS);
        when(mockDataManager.doValidate(any(), any(), Mockito.eq("3"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_FAILURE);
        when(mockDataManager.doValidate(any(), any(), Mockito.eq("4"), any(), any(), anyBoolean())).thenReturn(REPORT_WITH_FAILURE);
        when(mockMetadataRepository.findAll(any(Specifications.class))).thenReturn(testData);

        UnpublishInvalidMetadataJob toTest = createToTest();

        toTest.performJob(mockServiceContext);

        ArgumentCaptor<List> impactedMetadaCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockMailSender).notifyOwners(impactedMetadaCaptor.capture());
        Assert.assertEquals(2, impactedMetadaCaptor.getValue().size());
    }



    private void initMock() {
        mockMetadataRepository = mock(MetadataRepository.class);
        mockMailSender = mock(MailSender.class);
        mockServiceContext = mock(ServiceContext.class);
        mockSettingManager = mock(SettingManager.class);
        mockDataManager = mock(DataManager.class);
        mockPublishRecordRepository = mock(PublishRecordRepository.class);
        mockXmlSerializer = mock(XmlSerializer.class);
        mockOperationAllowedRepository = mock(OperationAllowedRepository.class);
        when(mockServiceContext.getBean(MetadataRepository.class)).thenReturn(mockMetadataRepository);
        when(mockSettingManager.getValueAsInt("system/metadata/publish_tracking_duration", 100)).thenReturn(100);
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
        toTest.setMailSender(mockMailSender);
        toTest.settingManager = mockSettingManager;
        toTest.dataManager = mockDataManager;
        toTest.publishRecordRepository = mockPublishRecordRepository;
        toTest.xmlSerializer = mockXmlSerializer;
        toTest.operationAllowedRepository = mockOperationAllowedRepository;
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
