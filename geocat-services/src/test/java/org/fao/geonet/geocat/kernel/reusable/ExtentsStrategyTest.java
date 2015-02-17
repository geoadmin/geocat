package org.fao.geonet.geocat.kernel.reusable;

import com.vividsolutions.jts.geom.Point;
import jeeves.server.UserSession;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.extent.FeatureType;
import org.fao.geonet.geocat.kernel.extent.Source;
import org.fao.geonet.geocat.services.reusable.AbstractSharedObjectTest;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Xml;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ExtentsStrategyTest extends AbstractSharedObjectTest {
    public static final String OTHERTYPE = "gn:othertype";
    public static final String ID = "id";
    public static final String THE_GEOM = "the_geom";
    public static final String SEARCH = "search";
    public static final String DESC = "desc";
    @Autowired
    ExtentManager extentManager;
    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;

    private MemoryDataStore dataStore;
    private TestSource source;

    @Before
    public void setUp() throws Exception {
        dataStore = new MemoryDataStore();
        source = new TestSource(dataStore);

        createFeatureType(dataStore, ExtentsStrategy.XLINK_TYPE);
        createFeatureType(dataStore, ExtentsStrategy.NON_VALIDATED_TYPE);
        createFeatureType(dataStore, OTHERTYPE);

        source.init();

        extentManager.setSource(source);
    }

    @Test
    public void testSearch() throws Exception {
        final ExtentsStrategy extentsStrategy = new ExtentsStrategy(geonetworkDataDirectory.getWebappDir(), extentManager, "eng");
        UserSession session = new UserSession();

        Element result = extentsStrategy.search(session, "bern", "eng", 10);
        assertEquals(Xml.getString(result), 3, result.getContentSize());

        result = extentsStrategy.search(session, "b2", "eng", 1);
        assertEquals(Xml.getString(result), 1, result.getContentSize());
    }


    @Test
    public void testList() throws Exception {
        final ExtentsStrategy extentsStrategy = new ExtentsStrategy(geonetworkDataDirectory.getWebappDir(), extentManager, "eng");
        UserSession session = new UserSession();

        Element result = extentsStrategy.list(session, null, "eng", 100);
        assertEquals(Xml.getString(result), 4, result.getContentSize());

        result = extentsStrategy.list(session, null, "eng", 2);
        assertEquals(Xml.getString(result), 2, result.getContentSize());

        result = extentsStrategy.list(session, null, "eng", 3);
        assertEquals(Xml.getString(result), 3, result.getContentSize());

        result = extentsStrategy.list(session, null, "eng", 1);
        assertEquals(Xml.getString(result), 1, result.getContentSize());

        result = extentsStrategy.list(session, SharedObjectStrategy.LUCENE_EXTRA_VALIDATED, "eng", 100);
        assertEquals(Xml.getString(result), 1, result.getContentSize());

        result = extentsStrategy.list(session, OTHERTYPE, "eng", 100);
        assertEquals(Xml.getString(result), 2, result.getContentSize());

        result = extentsStrategy.list(session, ExtentsStrategy.XLINK_TYPE, "eng", 100);
        assertEquals(Xml.getString(result), 1, result.getContentSize());

        result = extentsStrategy.list(session, ExtentsStrategy.NON_VALIDATED_TYPE, "eng", 100);
        assertEquals(Xml.getString(result), 1, result.getContentSize());

        result = extentsStrategy.list(session, SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED, "eng", 100);
        assertEquals(Xml.getString(result), 1, result.getContentSize());
    }



    private void createFeatureType(MemoryDataStore dataStore, String featureTypeName) throws IOException {
        FeatureType featureTypeDef = new FeatureType();
        featureTypeDef.setSource(source);
        featureTypeDef.setDescColumn(DESC);
        featureTypeDef.setGeoIdColumn(ID);
        featureTypeDef.setIdColumn(ID);
        featureTypeDef.setSearchColumn(SEARCH);
        featureTypeDef.setSrs("EPSG:4326");
        featureTypeDef.setTypename(featureTypeName);
        featureTypeDef.init();

        SimpleFeatureTypeBuilder ft = new SimpleFeatureTypeBuilder();
        ft.setName(featureTypeDef.pgTypeName);
        ft.add(THE_GEOM, Point.class, 4326);
        ft.add(SEARCH, String.class);
        ft.add(DESC, String.class);
        ft.add(ID, Integer.class);

        SimpleFeatureType featureType = ft.buildFeatureType();
        dataStore.createSchema(featureType);

        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        builder.set(THE_GEOM, null);
        builder.set(DESC, "bern" + featureTypeName);
        builder.set(SEARCH, "bern" + featureTypeName);
        builder.set(ID, 1);
        final SimpleFeature feature1 = builder.buildFeature("1" + featureTypeName);
        dataStore.addFeature(feature1);

        if (featureTypeName.equalsIgnoreCase(OTHERTYPE)) {
            builder.set(THE_GEOM, null);
            builder.set(DESC, "b2" + featureTypeName);
            builder.set(SEARCH, "b2" + featureTypeName);
            builder.set(ID, 2);
            final SimpleFeature feature2 = builder.buildFeature("2" + featureTypeName);
            dataStore.addFeature(feature2);
        }

        source.getTypeDefinitions().put(featureTypeDef.pgTypeName, featureTypeDef);
    }

    private static class TestSource extends Source {

        public TestSource(MemoryDataStore dataStore) {
            this.datastore = dataStore;
            this.wfsId = "DEFAULT";
        }

        @Override
        public void init() {
            super.init();
        }
    }
}