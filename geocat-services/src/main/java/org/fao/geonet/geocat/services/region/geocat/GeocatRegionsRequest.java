package org.fao.geonet.geocat.services.region.geocat;


import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.fao.geonet.domain.geocat.GeomTableLastModified;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.Request;
import org.fao.geonet.repository.geocat.GeomTableLastModifiedRepository;
import org.opengis.filter.Filter;
import org.opengis.filter.MultiValuedFilter.MatchAction;
import org.opengis.filter.Or;
import org.opengis.filter.expression.PropertyName;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static org.fao.geonet.geocat.services.region.geocat.DatastoreMapper.SEARCH;

public class GeocatRegionsRequest extends Request {

    private final GeomTableLastModifiedRepository lastModifiedRepository;
    Set<String> labels = new HashSet<String>();
    Set<String> categoryIds = new HashSet<String>();
    Multimap<DatastoreMapper, Filter> ids = HashMultimap.create();
    private int maxRecords = Integer.MAX_VALUE;
    private boolean all = true;
    private final MapperState state;

    public GeocatRegionsRequest(MapperState state, GeomTableLastModifiedRepository lastModifiedRepository) {
        this.state = state;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public Request label(String labelParam) {
        all = false;
        labels.add(labelParam);
        return this;
    }

    @Override
    public Request categoryId(String categoryIdParam) {
        all = false;
        categoryIds.add(categoryIdParam);
        return this;
    }

    @Override
    public Request maxRecords(int maxRecordsParam) {
        this.maxRecords = maxRecordsParam;
        return this;
    }

    @Override
    public Request id(String regionId) {
        all = false;
        DatastoreMapper mapper = DatastoreMappers.find(regionId);
        this.ids.put(mapper, mapper.idFilter(state, regionId));

        return this;
    }

    @Override
    public Optional<Long> getLastModified() throws Exception {
        Optional<Long> lastModified = Optional.absent();
         Collection<String> regionAndCategoryIds = getRegionIdFromIdAndCategory();
        for (String regionAndCategoryId : regionAndCategoryIds) {
            DatastoreMapper datastoreMapper = null;
            for (DatastoreMappers mappers : DatastoreMappers.values()) {
                if (mappers.mapper.categoryId().equalsIgnoreCase(regionAndCategoryId) || mappers.mapper.accepts(regionAndCategoryId)) {
                    datastoreMapper = mappers.mapper;
                    break;
                }
            }
            if (datastoreMapper == null) {
                continue;
            }
            String[] tableNames = {
                    datastoreMapper.getBackingDatastoreName(true, true),
                    datastoreMapper.getBackingDatastoreName(true, false),
                    datastoreMapper.getBackingDatastoreName(false, true),
                    datastoreMapper.getBackingDatastoreName(false, false),
            };
            final List<GeomTableLastModified> lastModifieds = this.lastModifiedRepository.findAll(Arrays.asList(tableNames));
            for (GeomTableLastModified modified : lastModifieds) {
                final long time = modified.getLastmodified().getTime();
                if (!lastModified.isPresent() || time > lastModified.get()) {
                    lastModified = Optional.of(time);
                }
            }
        }
        return lastModified;

    }

    private Collection<String> getRegionIdFromIdAndCategory() {
        Set<String> regions = Sets.newHashSet();
        regions.addAll(this.categoryIds);
        for (DatastoreMapper mapper : this.ids.keySet()) {
            regions.add(mapper.categoryId());
        }
        return regions;
    }

    @Override
    public Collection<Region> execute() throws Exception {
        Collection<Region> results = new LinkedList<Region>();
        if (all) {
            for (DatastoreMappers mapper : DatastoreMappers.values()) {
                categoryIds.add(mapper.mapper.categoryId());
            }
        }
        if (!categoryIds.isEmpty() && labels.isEmpty()) {
            for (DatastoreMappers mapper : DatastoreMappers.values()) {
                if (categoryIds.contains(mapper.mapper.categoryId())) {
                    mapper.mapper.loadRegions(state, results, maxRecords, Filter.INCLUDE);
                }
            }
        } else if (labels.isEmpty() && !(ids.isEmpty())) {
            // only ids so we can target the correct feature source directly and be more performant
            for (Entry<DatastoreMapper, Collection<Filter>> entry : ids.asMap().entrySet()) {
                Or filter = state.filterFactory.or(new LinkedList<Filter>(entry.getValue()));
                entry.getKey().loadRegions(state, results, maxRecords, filter);
            }
        } else {
            java.util.List<Filter> filters = new LinkedList<Filter>();
            for (Entry<DatastoreMapper, Collection<Filter>> entry : ids.asMap().entrySet()) {
                Or filter = state.filterFactory.or(new LinkedList<Filter>(entry.getValue()));
                filters.add(filter);
            }
            for (String label : labels) {
                PropertyName expr = state.filterFactory.property(SEARCH);
                filters.add(state.filterFactory.like(expr, "*" + label + "*", "*", "?", "\\", false, MatchAction.ANY));
            }
            Filter filter = state.filterFactory.or(filters);

            for (DatastoreMappers mapper : DatastoreMappers.values()) {
                if (categoryIds.isEmpty() || categoryIds.contains(mapper.mapper.categoryId())) {
                    mapper.mapper.loadRegions(state, results, maxRecords, filter);
                }
            }
        }
        return results;
    }

}
