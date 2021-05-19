package org.fao.geonet.kernel.extent;

import org.fao.geonet.utils.Log;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.logging.Logging;
import org.jdom.Namespace;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static org.fao.geonet.kernel.extent.ExtentHelper.encodeDescription;
import static org.fao.geonet.kernel.extent.ExtentHelper.reduceDesc;

/**
 * The configuration object for Extents. It allows access to the Datastore(s)
 * for obtaining the extents
 *
 * @author jeichar
 */
public class ExtentManager {

    private static final java.util.logging.Logger LOGGER = Logging.getLogger("org.geotools.data");
    public static final Namespace GMD_NAMESPACE    = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GCO_NAMESPACE    = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    public static final String GEOTOOLS_LOG_NAME = "geotools";

    @Autowired
    private DataStore datastore;
    private Source source;

    public DataStore getDataStore() throws IOException {
        return source.getDataStore();
    }

    public Source getSource() {
        return source;
    }

    public String add(String id, final String geoId, final String desc, final String requestCrsCode,
                      final FeatureType featureType, final FeatureStore<SimpleFeatureType, SimpleFeature> store,
                      Geometry geometry, boolean showNative) throws Exception{
        final SimpleFeatureType schema = store.getSchema();
        geometry = ExtentHelper.prepareGeometry(requestCrsCode, featureType, geometry, schema);

        id = addFeature(id, geoId, desc, geometry, featureType, store, schema, showNative);
        return id;
    }

    @SuppressWarnings("deprecation")
    private String addFeature(String id, String geoId, String desc, Geometry geometry, FeatureType featureType,
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

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        featureCollection.add(feature);
        store.addFeatures(featureCollection);
        return id;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    private static final class SourcesLogHandler extends Handler {

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
    }}
