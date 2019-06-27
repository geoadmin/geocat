package org.fao.geonet.kernel.url;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.UpdateDatestamp.NO;

public class UrlAnalyserTest extends AbstractCoreIntegrationTest {


    private static final int TEST_OWNER = 42;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private LinkRepository linkRepository;

    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
    }

    @Test
    public void encounteringAnUrlForTheFirstTimeAndPersistingIt() throws Exception {
        URL mdResourceUrl = UrlAnalyserTest.class.getResource("input_with_url.xml");
        Element element = Xml.loadStream(mdResourceUrl.openStream());
        AbstractMetadata md = insertMetadataInDb(element);


        SchemaPlugin schemaPlugin = schemaManager.getSchema(md.getDataInfo().getSchemaId()).getSchemaPlugin();
        if (schemaPlugin instanceof LinkAwareSchemaPlugin) {

            RawLinkPatternStreamer<Link> patternStreamer = ((LinkAwareSchemaPlugin) schemaPlugin).create(new ILinkBuilder<Link>() {
                @Override
                public Link build() {
                    return new Link();
                }

                @Override
                public void setUrl(Link link, String url) {
                    link.setUrl(url);
                }
            });

            patternStreamer.results(element).forEach(link -> {linkRepository.save(link);});

        }

        List<Link> fromDb = linkRepository.findAll();
        System.out.println(fromDb);

    }
    private AbstractMetadata insertMetadataInDb(Element element) throws Exception {
        loginAsAdmin(context);

        Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(element)
                .setUuid(UUID.randomUUID().toString());
        metadata.getDataInfo()
                .setRoot(element.getQualifiedName())
                .setSchemaId(schemaManager.autodetectSchema(element))
                .setType(MetadataType.METADATA)
                .setPopularity(1000);
        metadata.getSourceInfo()
                .setOwner(TEST_OWNER)
                .setSourceId(sourceRepository.findAll().get(0).getUuid());
        metadata.getHarvestInfo()
                .setHarvested(false);

        AbstractMetadata dbInsertedMetadata = dataManager.insertMetadata(
                context,
                metadata,
                element,
                false,
                true,
                false,
                NO,
                false,
                false);

        return dbInsertedMetadata;
    }



}
