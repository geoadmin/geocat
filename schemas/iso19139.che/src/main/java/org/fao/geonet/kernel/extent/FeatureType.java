package org.fao.geonet.kernel.extent;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.GeoTools;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;

/**
* @author Jesse on 10/28/2014.
*/
public final class FeatureType {
    public static final String SHOW_NATIVE = "SHOW_NATIVE";

    private CoordinateReferenceSystem projection;
    public String               typename;
    public String               idColumn;
    public String               geoIdColumn;
    public String               descColumn;
    public String               searchColumn;
    public String               showNativeColumn = SHOW_NATIVE;
    public String               pgTypeName;
    private String              srs;
    private Source              source;

    @PostConstruct
    public void init() {
        this.pgTypeName = typename.substring(3);
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public void setGeoIdColumn(String geoIdColumn) {
        this.geoIdColumn = geoIdColumn;
    }

    public void setDescColumn(String descColumn) {
        this.descColumn = descColumn;
    }

    public void setSearchColumn(String searchColumn) {
        this.searchColumn = searchColumn;
    }

    public synchronized void setSrs(String srs) {
        this.srs = srs;
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource() throws IOException {
        final DataStore datastore = source.getDataStore();
        if (Arrays.asList(datastore.getTypeNames()).contains(pgTypeName)) {
            return datastore.getFeatureSource(pgTypeName);
        } else {
            return null;
        }
    }

    public boolean isModifiable()
    {
        return source.modifiable.contains(this);
    }

    @Override
    public String toString() {

        String string = "typename (" + idColumn + "," + descColumn + ")";

        if (isModifiable()) {
            string += "modifiable";
        }

        return string;
    }

    public Query createQuery(String id, String[] properties)
    {
        final Filter filter = createFilter(id);
        return new Query(pgTypeName, filter, properties);
    }
    public Query createQuery(Filter filter, String[] properties) {
        return new Query(pgTypeName, filter, properties);
    }
    public Query createQuery(String[] properties) {
        return createQuery(Filter.INCLUDE,properties);
    }
    public Filter createFilter(String id)
    {
        final FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        final Literal literal = factory.literal(id);
        final PropertyName property = factory.property(idColumn);
        final Filter filter = factory.equals(property, literal);
        return filter;
    }

    public Source wfs()
    {
        return this.source;
    }

    public synchronized CoordinateReferenceSystem projection()
    {
        try {
            this.projection = CRS.decode(srs);
        } catch (Exception e) {
            this.projection = DefaultGeographicCRS.WGS84;
            this.srs = "EPSG:4326";
        }
        return projection;
    }

    public synchronized String srs()
    {
        return srs;
    }
}
