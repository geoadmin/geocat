package org.fao.geonet.kernel.search;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

import java.io.Closeable;
import java.io.IOException;

public class IndexAndTaxonomy implements Closeable {
    public final TaxonomyReader taxonomyReader;
    public final GeonetworkMultiReader indexReader;
    public final long version;
    public final String preferredLang;

    public IndexAndTaxonomy(String preferredLang, long version, GeonetworkMultiReader indexReader, TaxonomyReader taxonomyReader) {
        this.preferredLang = preferredLang;
        this.taxonomyReader = taxonomyReader;
        this.indexReader = indexReader;
        this.version = version;
    }

    @Override
    public void close() throws IOException {
        indexReader.releaseToNRTManager();
    }
}
