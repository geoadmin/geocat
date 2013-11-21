package org.fao.geonet.geocat.kernel.extent;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.utils.Log;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.logging.Logging;
import org.jdom.Element;
import org.jdom.Namespace;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.*;

/**
 * The configuration object for Extents. It allows access to the Datastore(s)
 * for obtaining the extents
 *
 * @author jeichar
 */
@Component
public class ExtentManager {

    private static final java.util.logging.Logger LOGGER = Logging.getLogger("org.geotools.data");
    public static final Namespace GMD_NAMESPACE    = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GCO_NAMESPACE    = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    public static final String GEOTOOLS_LOG_NAME = "geotools";

    @Autowired
    private DataStore datastore;


    private final class SourcesLogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            Log.debug(GEOTOOLS_LOG_NAME, record.getMessage());
        }

        @Override
        public void flush() {
            // nothing
        }

        @Override
        public void close() throws SecurityException {
            // nothing

        }
    }

    private final Map<String, Source> sources = new HashMap<String, Source>();


    public void init(java.util.List<Element> extentConfig) throws Exception {
        if (Logger.getLogger(GEOTOOLS_LOG_NAME).isDebugEnabled()) {
            LOGGER.setLevel(java.util.logging.Level.FINE);
            LOGGER.addHandler(new SourcesLogHandler());
        }
        if (extentConfig == null) {
            Log.error(Geocat.Module.EXTENT, "No Extent configuration found.");
        } else {
            Element sourceElem = extentConfig.get(0);

                String id = sourceElem.getAttributeValue(ID);
                if (id == null) {
                    id = DEFAULT_SOURCE_ID;
                }

                final Source source = new Source(id);

                sources.put(id, source);

                source.datastore = this.datastore;

                for (final Object obj : sourceElem.getChildren(TYPENAME)) {
                    final Element elem = (Element) obj;
                    final String typename = elem.getAttributeValue(TYPENAME);
                    final String idColumn = elem.getAttributeValue(ID_COLUMN);
                    if (idColumn == null) {
                        throw new Exception("the idColumn attribute for extent source configuration " + typename +"is missing");
                    }

                    final String projection = elem.getAttributeValue("CRS");
                    final String descColumn = elem.getAttributeValue(DESC_COLUMN);
                    final String geoIdColumn = elem.getAttributeValue(GEO_ID_COLUMN);
                    final String searchColumn = elem.getAttributeValue("searchColumn");
                    final String modifiable = elem.getAttributeValue(MODIFIABLE_FEATURE_TYPE);

                    source.addFeatureType(typename, idColumn, geoIdColumn, descColumn, searchColumn, projection, "true"
                            .equalsIgnoreCase(modifiable));
                }

        }

    }

    public DataStore getDataStore() throws IOException {
        return sources.get(DEFAULT_SOURCE_ID).getDataStore();
    }

    public DataStore getDataStore(String id) throws IOException {
        final String concId = id == null ? DEFAULT_SOURCE_ID : id;
        return sources.get(concId).getDataStore();
    }

    public Map<String, Source> getSources() {
        return sources;
    }

    public Source getSource(String source) {
        if (source == null) {
            return sources.get(DEFAULT_SOURCE_ID);
        }
        return sources.get(source);
    }

    public Source getSource() {
        return sources.get(DEFAULT_SOURCE_ID);
    }


    public String add(String id, final String geoId, final String desc, final String requestCrsCode,
                             final Source.FeatureType featureType, final FeatureStore<SimpleFeatureType, SimpleFeature> store, Geometry geometry, boolean showNative)
            throws Exception
    {
        final SimpleFeatureType schema = store.getSchema();
        geometry = ExtentHelper.prepareGeometry(requestCrsCode, featureType, geometry, schema);

        id = addFeature(id, geoId, desc, geometry, featureType, store, schema, showNative);
        return id;
    }

    @SuppressWarnings("deprecation")
    private String addFeature(String id, String geoId, String desc, Geometry geometry, Source.FeatureType featureType,
                                     FeatureStore<SimpleFeatureType, SimpleFeature> store, SimpleFeatureType schema, boolean showNative) throws Exception
    {

        if (id == null) {
            id = ExtentHelper.findNextId(store, featureType);
        }

        final SimpleFeature feature = SimpleFeatureBuilder.template(schema, SimpleFeatureBuilder
                .createDefaultFeatureId());
        feature.setAttribute(featureType.idColumn, id);
        feature.setAttribute(featureType.geoIdColumn, encodeDescription(geoId));
        feature.setAttribute(featureType.descColumn, encodeDescription(desc));
        feature.setAttribute(featureType.showNativeColumn, showNative?"y":"n");
        feature.setAttribute(featureType.searchColumn, encodeDescription(reduceDesc(desc) + reduceDesc(geoId)));
        feature.setDefaultGeometry(geometry);

        final FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
        collection.add(feature);
        store.addFeatures(collection);
        return id;
    }}
