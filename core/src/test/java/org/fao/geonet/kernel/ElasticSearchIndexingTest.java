package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import static org.fao.geonet.domain.MetadataType.*;
import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.junit.Assert.*;


public class ElasticSearchIndexingTest extends AbstractIntegrationTestWithMockedSingletons {

    private static final int TEST_OWNER_ID = 42;

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private SettingManager settingManager;

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
        resetAndGetMockInvoker();
    }
    @Test
    public void complexDatesAreIndexedCheck() throws Exception {
        // GIVEN
        loadMetadataWithTemporalExtentUsingSimpleDates();
        URL dateResource = AbstractCoreIntegrationTest.class.getResource("kernel/holocene_che.xml");
        Element dateElement = Xml.loadStream(Objects.requireNonNull(dateResource).openStream());

        // WHEN
        AbstractMetadata dbInsertedMetadata = insertTemplateResourceInDb(dateElement);

        //THEN
        SearchResponse response = this.searchManager.query("_id:" + dbInsertedMetadata.getUuid() + " AND resourceTitleObject.default:holocene", null, 0, 10);
        boolean hasAtLeastOneHit = response.getHits().getTotalHits().value == 1;
        assertTrue(String.format("Did not index the holocene data with complex date due to: %s et %s", response, dbInsertedMetadata), hasAtLeastOneHit);
    }

    private void loadMetadataWithTemporalExtentUsingSimpleDates() throws Exception {
        URL dateResource = AbstractCoreIntegrationTest.class.getResource("kernel/forest_che.xml");
        Element dateElement = Xml.loadStream(Objects.requireNonNull(dateResource).openStream());
        insertTemplateResourceInDb(dateElement);
    }

    private AbstractMetadata insertTemplateResourceInDb(Element element) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(element)
            .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
            .setRoot(element.getQualifiedName())
            .setSchemaId(schemaManager.autodetectSchema(element))
            .setType(METADATA)
            .setPopularity(1000);
        metadata.getSourceInfo()
            .setOwner(TEST_OWNER_ID)
            .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
            .setHarvested(false);

        return metadataManager.insertMetadata(
            context,
            metadata,
            element,
            IndexingMode.full,
            false,
            NO,
            false,
            true);
    }
}
