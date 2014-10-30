package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.Constants;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.geocat.services.reusable.AbstractSharedObjectTest;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jesse on 10/27/2014.
 */
public abstract class AbstractSharedObjectStrategyTest extends AbstractSharedObjectTest {

    @Autowired
    private SearchManager searchManager;

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

    protected abstract Metadata createDefaultSubtemplate(boolean validated) throws Exception;

    protected abstract String getIsValidatedSpecificData();

    protected abstract ReplacementStrategy createReplacementStrategy();

    protected abstract Element createMetadata(Element formatXml);

}
