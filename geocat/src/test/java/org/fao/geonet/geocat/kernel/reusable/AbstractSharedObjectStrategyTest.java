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

import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_UUID_FIELD;
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
    public void testFindOnDeletedXLink() throws Exception {
        final Metadata subtemplate = createDefaultSubtemplate(false);
        final Element subtemplateXmlData = subtemplate.getXmlData(false);
        Element mdToProcess = createMetadata(subtemplateXmlData);
        subtemplateXmlData.getParentElement().setAttribute(XLink.HREF, "local://xml.reusable.deleted?id=142", XLink.NAMESPACE_XLINK);
        subtemplateXmlData.getParentElement().setAttribute(XLink.SHOW, "embed", XLink.NAMESPACE_XLINK);
        subtemplateXmlData.getParentElement().setAttribute(XLink.TITLE, "rejected", XLink.NAMESPACE_XLINK);

        final ReplacementStrategy replacementStrategy = createReplacementStrategy();

        assertSame(ReplacementStrategy.NULL, replacementStrategy.find(subtemplateXmlData.getParentElement(), subtemplateXmlData, "en"));

        long numMd = this.metadataRepository.count();

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, mdToProcess,
                mdToProcess, false, null, context);
        manager.process(params);

        assertEquals(numMd, this.metadataRepository.count());

    }

    @Test
    public void testPerformDelete() throws Exception {
        final Metadata sharedObj = createDefaultSubtemplate(true);

        final MetadataRepository bean = _applicationContext.getBean(MetadataRepository.class);
        long count = bean.count();
        final ReplacementStrategy replacementStrategy = createReplacementStrategy();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        int numberFormats = replacementStrategy.list(serviceContext.getUserSession(), true, "eng").getChildren().size();

        replacementStrategy.performDelete(new String[]{sharedObj.getUuid()}, serviceContext.getUserSession(), null);

        assertEquals(count - 1, bean.count());

        final List formats = replacementStrategy.list(serviceContext.getUserSession(), true, "eng").getChildren();

        assertEquals(numberFormats  - 1 , formats.size());

    }


    @Test
    public void testCreateFindMetadataQuery() throws Exception {
        final Metadata sharedObj = createDefaultSubtemplate(true);

        final ReplacementStrategy replacementStrategy = createReplacementStrategy();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final Element metadata = createMetadata(sharedObj.getXmlData(false));
        ByteArrayInputStream mdStream = new ByteArrayInputStream(Xml.getString(metadata).getBytes(Constants.CHARSET));
        final int mdId = importMetadataXML(serviceContext, "metadataUUID", mdStream, MetadataType.METADATA,
                ReservedGroup.all.getId(), "REPLACE");

        final IndexAndTaxonomy indexAndTaxonomy = this.searchManager.getNewIndexReader("eng");
        final IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
        String field = replacementStrategy.getValidXlinkLuceneField();
        final Query query = replacementStrategy.createFindMetadataQuery(field, sharedObj.getUuid(), true);
        final TopDocs mdFound = searcher.search(query, 1000);
        assertEquals(1, mdFound.totalHits);
        assertEquals(String.valueOf(mdId), searcher.doc(mdFound.scoreDocs[0].doc).get("_id"));
    }


    @Test
    public void testMarkAsValidated() throws Exception {
        final Metadata sharedObj = createDefaultSubtemplate(false);
        assertEquals(ReplacementStrategy.LUCENE_EXTRA_NON_VALIDATED, sharedObj.getDataInfo().getExtra());

        final ReplacementStrategy replacementStrategy = createReplacementStrategy();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        replacementStrategy.markAsValidated(new String[]{sharedObj.getUuid()}, serviceContext.getUserSession());

        final MetadataRepository bean = _applicationContext.getBean(MetadataRepository.class);
        final Metadata updated = bean.findOne(sharedObj.getId());
        assertEquals(ReplacementStrategy.LUCENE_EXTRA_VALIDATED, updated.getDataInfo().getExtra());
    }


    @Test
    public void testIsValidated() throws Exception {
        final ReplacementStrategy strategy = createReplacementStrategy();
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
        final ReplacementStrategy replacementStrategy = createReplacementStrategy();
        String href = replacementStrategy.createXlinkHref("", userSession, null);
        final String updatedHref = replacementStrategy.createAsNeeded(href, userSession);
        String uuid = Utils.id(updatedHref);

        assertEquals(count + 1, bean.count());

        assertNotNull(bean.findOneByUuid(uuid));

        final String updatedHref2 = replacementStrategy.createAsNeeded(updatedHref, userSession);

        assertEquals(updatedHref, updatedHref2);
    }

    public static void assertCorrectMetadataInLucene(ApplicationContext applicationContext, Query query, String... expectedUuids) throws IOException, InterruptedException {
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

    protected abstract Metadata createDefaultSubtemplate(boolean validated) throws Exception;

    protected abstract String getIsValidatedSpecificData();

    protected abstract ReplacementStrategy createReplacementStrategy();

    protected abstract Element createMetadata(Element formatXml);

}
