package org.fao.geonet.geocat.services.region.geocat;

import jeeves.server.context.ServiceContext;
import org.opengis.filter.FilterFactory2;

import java.util.Map;
import java.util.WeakHashMap;

public class MapperState {
    final ServiceContext context;
    final WeakHashMap<String, Map<String, String>> categoryIdMap;
    final FilterFactory2 filterFactory;
    final DatastoreCache datastoreCache;

    MapperState(ServiceContext context,
                WeakHashMap<String, Map<String, String>> categoryIdMap,
                FilterFactory2 filterFactory, DatastoreCache datastoreCache) {
        super();
        this.context = context;
        this.categoryIdMap = categoryIdMap;
        this.filterFactory = filterFactory;
        this.datastoreCache = datastoreCache;
    }


}
