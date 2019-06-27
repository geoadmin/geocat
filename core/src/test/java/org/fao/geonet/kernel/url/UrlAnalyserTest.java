package org.fao.geonet.kernel.url;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.fao.geonet.kernel.UpdateDatestamp.NO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UrlAnalyserTest extends AbstractCoreIntegrationTest {


    private static final int TEST_OWNER = 42;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private LinkRepository linkRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
        UrlAnalyser toTest = createToTest();

        toTest.processMetadata(element, md);

        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertTrue(urlFromDb.contains("HTTPS://acme.de/"));
        assertTrue(urlFromDb.contains("ftp://mon-site.mondomaine/mon-repertoire"));
        assertTrue(urlFromDb.contains("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif"));
        assertTrue(urlFromDb.contains("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif"));
        assertEquals(4, urlFromDb.size());
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(
                metadataLinkList.stream().map(x -> x.getId().getLinkId()).collect(Collectors.toSet()),
                linkRepository.findAll().stream().map(Link::getId).collect(Collectors.toSet()));
        assertEquals(
                metadataLinkList.stream().map(x -> x.getId().getMetadataId()).collect(Collectors.toSet()),
                Collections.singleton(md.getId()));
    }


    @Test
    public void encounteringSameUrlInVariousMd() throws Exception {
        URL mdResourceUrl = UrlAnalyserTest.class.getResource("input_with_url.xml");
        Element element = Xml.loadStream(mdResourceUrl.openStream());
        AbstractMetadata mdOne = insertMetadataInDb(element);
        AbstractMetadata mdTwo = insertMetadataInDb(element);
        UrlAnalyser toTest = createToTest();

        toTest.processMetadata(element, mdOne);
        toTest.processMetadata(element, mdTwo);

        Set<String> urlFromDb = linkRepository.findAll().stream().map(Link::getUrl).collect(Collectors.toSet());
        assertEquals(4, urlFromDb.size());
        SimpleJpaRepository metadataLinkRepository = new SimpleJpaRepository<MetadataLink, Integer>(MetadataLink.class, entityManager);
        List<MetadataLink> metadataLinkList = metadataLinkRepository.findAll();
        assertEquals(
                metadataLinkList.stream().map(x -> x.getId().getLinkId()).collect(Collectors.toSet()),
                linkRepository.findAll().stream().map(Link::getId).collect(Collectors.toSet()));
        assertEquals(
                metadataLinkList.stream().map(x -> x.getId().getMetadataId()).collect(Collectors.toSet()),
                Stream.of(mdOne.getId(), mdTwo.getId()).collect(Collectors.toSet()));
        assertEquals(8, metadataLinkList.size());
    }


    private UrlAnalyser createToTest() {
        UrlAnalyser toTest = new UrlAnalyser();
        toTest.schemaManager = schemaManager;
        toTest.metadataRepository = metadataRepository;
        toTest.entityManager = entityManager;
        return toTest;
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
