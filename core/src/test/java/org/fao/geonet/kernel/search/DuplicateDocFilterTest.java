package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DuplicateDocFilterTest extends AbstractCoreIntegrationTest {
    @Autowired
    private SearchManager searchManager;

    @Test
    public void testRemoveDuplicateDocs() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final URL resource = DuplicateDocFilterTest.class.getResource("multi-lingual.xml");
        importMetadataXML(context, UUID.randomUUID().toString(), resource.openStream(),
                MetadataType.METADATA, 1, Params.GENERATE_UUID);


        final String preferredLang = "eng";
        try (IndexAndTaxonomy indexAndTaxonomy = searchManager.openIndexReader(preferredLang, -1)) {
            final IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
            TopDocs searchResults = searcher.search(new MatchAllDocsQuery(), 10000);

            assertEquals(5, searchResults.totalHits);

            DuplicateDocFilter duplicateDocFilter = this.searchManager.createDuplicateDocFilter(preferredLang, new MatchAllDocsQuery());
            searchResults = searcher.search(new MatchAllDocsQuery(), duplicateDocFilter, 10000);

            assertEquals(1, searchResults.totalHits);

        }
    }
}