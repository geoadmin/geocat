package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.collect.Sets;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.Constants;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.geocat.services.reusable.AbstractSharedObjectTest;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_UUID_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 10/27/2014.
 */
public abstract class AbstractSharedObjectStrategyTest extends AbstractSharedObjectTest {

    @Autowired
    protected ReusableObjManager manager;
    @Autowired
    protected MetadataRepository metadataRepository;
    @Autowired
    protected SearchManager searchManager;

    @Test
    public void testSearch() throws Exception {
        String searchTerm = "searchTerm";

        createDefaultSubtemplate(searchTerm, false);
        createDefaultSubtemplate(searchTerm+"Valid", true);
        createDefaultSubtemplate("xxxxx", true);

        final SharedObjectStrategy strategy = createReplacementStrategy();
        UserSession session = new UserSession();
        Element search = strategy.search(session, null, searchTerm, "eng", 10);

        assertEquals(2, search.getContentSize());
        assertEquals("true", ((Element)search.getChildren().get(0)).getChildText(SharedObjectStrategy.REPORT_VALIDATED));
        assertEquals("false", ((Element)search.getChildren().get(1)).getChildText(SharedObjectStrategy.REPORT_VALIDATED));
        for (Object o : search.getChildren()) {
            Element e = (Element) o;
            assertTrue(e.getChildText(SharedObjectStrategy.REPORT_DESC).contains(searchTerm));
        }

        search = strategy.search(session, null, searchTerm, "eng", 1);
        assertEquals(1, search.getContentSize());
        assertEquals("true", ((Element)search.getChildren().get(0)).getChildText(SharedObjectStrategy.REPORT_VALIDATED));
    }

    @Test
    public void testFindOnDeletedXLink() throws Exception {
        final Metadata subtemplate = createDefaultSubtemplate(false);
        final Element subtemplateXmlData = subtemplate.getXmlData(false);
        Element mdToProcess = createMetadata(subtemplateXmlData);
        final Element linkedElement = subtemplateXmlData.getParentElement();
        linkedElement.setAttribute(XLink.HREF, "local://xml.reusable.deleted?id=142", XLink.NAMESPACE_XLINK);
        linkedElement.setAttribute(XLink.SHOW, "embed", XLink.NAMESPACE_XLINK);
        linkedElement.setAttribute(XLink.TITLE, "rejected", XLink.NAMESPACE_XLINK);

        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();

        assertSame(SharedObjectStrategy.NULL, sharedObjectStrategy.find(new Element("placeholder"), linkedElement, "en"));

        long numMd = this.metadataRepository.count();

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, mdToProcess,
                mdToProcess, false, null, context);
        manager.process(params);

        assertEquals(numMd, this.metadataRepository.count());

    }

    @Test
    public void testPerformDelete() throws Exception {
        final String validated = SharedObjectStrategy.LUCENE_EXTRA_VALIDATED;
        final Metadata sharedObj = createDefaultSubtemplate(true);

        final MetadataRepository bean = _applicationContext.getBean(MetadataRepository.class);
        long count = bean.count();
        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        int numberFormats = sharedObjectStrategy.list(serviceContext.getUserSession(), validated, "eng", 30000).getChildren().size();

        final String mdUUID = sharedObj.getUuid();
        sharedObjectStrategy.performDelete(new String[]{mdUUID}, serviceContext.getUserSession(), null);

        assertEquals(count - 1, bean.count());

        final List formats = sharedObjectStrategy.list(serviceContext.getUserSession(), validated, "eng", 30000).getChildren();

        assertEquals(numberFormats - 1, formats.size());

        final Element list = sharedObjectStrategy.list(serviceContext.getUserSession(), validated, "eng", 30000);
        assertEquals(0, list.getChildren().size());
    }

    @Test
    public void testCreateFindMetadataQuery() throws Exception {
        final Metadata sharedObj = createDefaultSubtemplate(true);

        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element metadata = createMetadata(sharedObj.getXmlData(false));
        ByteArrayInputStream mdStream = new ByteArrayInputStream(Xml.getString(metadata).getBytes(Constants.CHARSET));
        final int mdId = importMetadataXML(serviceContext, "metadataUUID", mdStream, MetadataType.METADATA,
                ReservedGroup.all.getId(), "REPLACE");

        final IndexAndTaxonomy indexAndTaxonomy = this.searchManager.getNewIndexReader("eng");
        final IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
        String field = sharedObjectStrategy.getValidXlinkLuceneField();
        final Query query = sharedObjectStrategy.createFindMetadataQuery(field, sharedObj.getUuid(), true);
        final TopDocs mdFound = searcher.search(query, 1000);
        assertEquals(1, mdFound.totalHits);
        assertEquals(String.valueOf(mdId), searcher.doc(mdFound.scoreDocs[0].doc).get("_id"));
    }

    @Test
    public void testMarkAsValidated() throws Exception {
        final String validated = LUCENE_EXTRA_NON_VALIDATED;
        final Metadata sharedObj = createDefaultSubtemplate(false);
        assertEquals(LUCENE_EXTRA_NON_VALIDATED, sharedObj.getDataInfo().getExtra());

        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element list = sharedObjectStrategy.list(serviceContext.getUserSession(), validated, "eng", 30000);
        assertEquals(1, list.getChildren().size());
        assertEqualsText(sharedObj.getUuid(), list, "record/id");
        sharedObjectStrategy.markAsValidated(new String[]{sharedObj.getUuid()}, serviceContext.getUserSession());

        final MetadataRepository bean = _applicationContext.getBean(MetadataRepository.class);
        final Metadata updated = bean.findOne(sharedObj.getId());
        assertEquals(SharedObjectStrategy.LUCENE_EXTRA_VALIDATED, updated.getDataInfo().getExtra());


        list = sharedObjectStrategy.list(serviceContext.getUserSession(), validated, "eng", 30000);
        assertEquals(0, list.getChildren().size());

        list = sharedObjectStrategy.list(serviceContext.getUserSession(), SharedObjectStrategy.LUCENE_EXTRA_VALIDATED, "eng", 30000);
        assertEquals(1, list.getChildren().size());
        assertEqualsText(sharedObj.getUuid(), list, "record/id");
    }

    @Test
    public void testList() throws Exception {
        createDefaultSubtemplate(false);
        createDefaultSubtemplate(false);
        createDefaultSubtemplate(false);
        createDefaultSubtemplate(true);
        createDefaultSubtemplate(true);

        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        Element list = sharedObjectStrategy.list(serviceContext.getUserSession(), null, "eng", 30000);
        assertEquals(5, list.getChildren().size());
        list = sharedObjectStrategy.list(serviceContext.getUserSession(), LUCENE_EXTRA_NON_VALIDATED, "eng", 30000);
        assertEquals(3, list.getChildren().size());
        list = sharedObjectStrategy.list(serviceContext.getUserSession(), LUCENE_EXTRA_VALIDATED, "eng", 30000);
        assertEquals(2, list.getChildren().size());

        list = sharedObjectStrategy.list(serviceContext.getUserSession(), null, "eng", 4);
        assertEquals(4, list.getChildren().size());
        list = sharedObjectStrategy.list(serviceContext.getUserSession(), null, "eng", 1);
        assertEquals(1, list.getChildren().size());
    }

    @Test
    public void testIsValidated() throws Exception {
        final SharedObjectStrategy strategy = createReplacementStrategy();
        final Metadata sharedObj1 = createDefaultSubtemplate(false);
        final Metadata sharedObj2 = createDefaultSubtemplate(true);

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();

        final String href = strategy.createXlinkHref(sharedObj1.getUuid(), userSession, getIsValidatedSpecificData());
        assertEquals(false, strategy.isValidated(href));


        final String href2 = strategy.createXlinkHref(sharedObj2.getUuid(), userSession, getIsValidatedSpecificData());
        assertEquals(true, strategy.isValidated(href2));
    }

    @Test
    public void testCreateAsNeeded() throws Exception {
        final MetadataRepository bean = _applicationContext.getBean(MetadataRepository.class);

        long count = bean.count();

        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final SharedObjectStrategy sharedObjectStrategy = createReplacementStrategy();
        String href = sharedObjectStrategy.createXlinkHref("", userSession, null);
        final String updatedHref = sharedObjectStrategy.createAsNeeded(href, userSession);
        String uuid = Utils.id(updatedHref);

        assertEquals(count + 1, bean.count());

        assertNotNull(bean.findOneByUuid(uuid));

        final String updatedHref2 = sharedObjectStrategy.createAsNeeded(updatedHref, userSession);

        assertEquals(updatedHref, updatedHref2);
    }

    public static void assertCorrectMetadataInLucene(ApplicationContext applicationContext, Query query,
                                                     String... expectedUuids) throws IOException, InterruptedException {
        final SearchManager searchManager = applicationContext.getBean(SearchManager.class);
        final IndexAndTaxonomy reader = searchManager.getNewIndexReader("eng");

        try {
            final IndexSearcher searcher = new IndexSearcher(reader.indexReader);
            final TopDocs search = searcher.search(query, 300);

            final ScoreDoc[] scoreDocs = search.scoreDocs;

            Set<String> uuids = Sets.newHashSet();
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                uuids.add(doc.get(LUCENE_UUID_FIELD));
            }

            assertEquals(expectedUuids.length, uuids.size());
            for (int i = 0; i < expectedUuids.length; i++) {
                String next = expectedUuids[i];
                assertTrue(next + " is missing", uuids.contains(next));
            }
        } finally {
            searchManager.releaseIndexReader(reader);
        }
    }

    protected abstract Metadata createDefaultSubtemplate(String seedData, boolean validated) throws Exception;
    protected Metadata createDefaultSubtemplate(boolean validated) throws Exception {
        return createDefaultSubtemplate(UUID.randomUUID().toString(), validated);
    }

    protected abstract String getIsValidatedSpecificData();

    protected abstract SharedObjectStrategy createReplacementStrategy();

    protected abstract Element createMetadata(Element formatXml);

}
