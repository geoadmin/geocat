package org.fao.geonet.kernel.search;

import bak.pcj.map.IntKeyMap;
import bak.pcj.map.IntKeyOpenHashMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the document id (in the searchers) to the id of the metadata so that the
 * {@link org.fao.geonet.kernel.search.DuplicateDocFilter} can efficiently filter out duplicates.
 *
 * @author Jesse on 1/4/2015.
 */
public class LuceneDocToIdTracker {

    private Map<String, MdIdLuceneDocIdMapper> searcherMap = new HashMap<>();

    synchronized void init(LuceneIndexLanguageTracker tracker) throws IOException {
        searcherMap.clear();
        try (IndexAndTaxonomy indexAndTaxonomy = tracker.acquire(null, -1)) {
            AtomicReader reader = SlowCompositeReaderWrapper.wrap(indexAndTaxonomy.indexReader);
            Terms terms = reader.terms(Geonet.IndexFieldNames.ID);
            if (terms != null) {
                TermsEnum enu = terms.iterator(null);
                BytesRef term = enu.next();
                while (term != null) {
                    add(tracker, term.utf8ToString());
                    term = enu.next();
                }
            }
        }
    }

    synchronized void remove(String metadataId) {
        for (MdIdLuceneDocIdMapper mdIdLuceneDocIdMapper : searcherMap.values()) {
            final Collection<Integer> docIds = mdIdLuceneDocIdMapper.mdIdMapToDocIdMap.removeAll(metadataId);
            for (Integer docId : docIds) {
                mdIdLuceneDocIdMapper.docIdToMdIdMap.remove(docId);
            }
        }
    }

    synchronized void add(LuceneIndexLanguageTracker tracker, String metadataId) throws IOException {
        BytesRef idBytes = new BytesRef(metadataId);
        final TermQuery idQuery = new TermQuery(new Term(Geonet.IndexFieldNames.ID, idBytes));

        Set<String> indices = tracker.indices();
        for (String index : indices) {
            MdIdLuceneDocIdMapper mdIdLuceneDocIdMapper = searcherMap.get(index);
            if (mdIdLuceneDocIdMapper == null) {
                mdIdLuceneDocIdMapper = new MdIdLuceneDocIdMapper();
                this.searcherMap.put(index, mdIdLuceneDocIdMapper);
            }

            try (IndexAndTaxonomy indexAndTaxonomy = tracker.acquire(index, -1)) {
                final IndexSearcher searcher = new IndexSearcher(indexAndTaxonomy.indexReader);
                final TopDocs searchResults = searcher.search(idQuery, Integer.MAX_VALUE);
                for (ScoreDoc scoreDoc : searchResults.scoreDocs) {
                    mdIdLuceneDocIdMapper.docIdToMdIdMap.put(scoreDoc.doc, metadataId);
                    mdIdLuceneDocIdMapper.mdIdMapToDocIdMap.put(metadataId, scoreDoc.doc);
                }
            }
        }
    }

    synchronized DuplicateDocFilter createDuplicateDocFilter(String preferredLang, Query query) {
        MdIdLuceneDocIdMapper mdIdLuceneDocIdMapper = this.searcherMap.get(preferredLang);
        return new DuplicateDocFilter(query, mdIdLuceneDocIdMapper);
    }
    static class MdIdLuceneDocIdMapper {
        IntKeyMap docIdToMdIdMap = new IntKeyOpenHashMap();
        Multimap<String, Integer> mdIdMapToDocIdMap = HashMultimap.create();
    }
}
