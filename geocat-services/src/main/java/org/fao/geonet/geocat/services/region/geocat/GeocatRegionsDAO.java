package org.fao.geonet.geocat.services.region.geocat;

import com.vividsolutions.jts.geom.Geometry;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.geocat.GeomTableLastModified;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.repository.geocat.GeomTableLastModifiedRepository;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeocatRegionsDAO extends RegionsDAO {

    @Autowired
    private GeomTableLastModifiedRepository lastModifiedRepository;
    private DatastoreCache datastoreCache = new DatastoreCache();
    private WeakHashMap<String, Map<String, String>> categoryIdMap = new WeakHashMap<String, Map<String, String>>();
    FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    @Override
    public Request createSearchRequest(ServiceContext context) throws Exception {
        MapperState state = new MapperState(context, categoryIdMap, filterFactory, datastoreCache);
        return new GeocatRegionsRequest(state);
    }

    @Override
    public Geometry getGeom(ServiceContext context, String regionId,
                            boolean simplified, CoordinateReferenceSystem projection) throws Exception {

        boolean isLatLong = CRS.equalsIgnoreMetadata(Region.WGS84, projection);
        try {
            MapperState state = new MapperState(context, categoryIdMap, filterFactory, datastoreCache);
            DatastoreMapper mapper = DatastoreMappers.find(regionId);
            Geometry geom = mapper.getGeometry(state, simplified, regionId, isLatLong);

            CoordinateReferenceSystem sourceSRS = (CoordinateReferenceSystem) geom.getUserData();
            Integer sourceCode = CRS.lookupEpsgCode(sourceSRS, false);
            Integer desiredCode = CRS.lookupEpsgCode(projection, false);
            if ((sourceCode == null || desiredCode == null || desiredCode.intValue() != sourceCode.intValue()) && !CRS
                    .equalsIgnoreMetadata(sourceSRS, projection)) {
                MathTransform transform = CRS.findMathTransform(sourceSRS, projection, true);
                geom = JTS.transform(geom, transform);
            }

            return geom;
        } catch (IllegalArgumentException e) {
            return null;
        }

    }

    @Override
    public boolean canHandleId(ServiceContext context, String id) throws Exception {
        for (DatastoreMappers datastoreMappers : DatastoreMappers.values()) {
            if (id.startsWith(datastoreMappers.mapper.categoryId() + ":")) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Long getLastModified(@Nonnull String id) throws Exception {
        final String[] split = id.split(":", 2);
        final DatastoreMapper datastoreMapper = DatastoreMappers.find(split[0]);
        String[] tableNames = {
                datastoreMapper.getBackingDatastoreName(true, true),
                datastoreMapper.getBackingDatastoreName(true, false),
                datastoreMapper.getBackingDatastoreName(false, true),
                datastoreMapper.getBackingDatastoreName(false, false),
        };
        long lastModified = -1;
        final List<GeomTableLastModified> lastModifieds = this.lastModifiedRepository.findAll(Arrays.asList(tableNames));
        for (GeomTableLastModified modified : lastModifieds) {
            final long time = modified.getLastmodified().getTime();
            if (time > lastModified) {
                lastModified = time;
            }
        }
        if (lastModified == -1) {
            return null;
        }
        return lastModified;
    }

    @Override
    public Collection<String> getRegionCategoryIds(ServiceContext context) {
        LinkedList<String> ids = new LinkedList<String>();
        for (DatastoreMappers mapper : DatastoreMappers.values()) {
            ids.add(mapper.mapper.categoryId());
        }
        return ids;
    }

}
