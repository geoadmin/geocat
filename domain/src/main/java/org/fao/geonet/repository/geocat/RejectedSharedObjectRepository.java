package org.fao.geonet.repository.geocat;

import org.fao.geonet.domain.geocat.RejectedSharedObject;
import org.fao.geonet.repository.GeonetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for accessing {@link RejectedSharedObject} objects.
 * User: Jesse
 * Date: 11/14/13
 * Time: 3:06 PM
 */
public interface RejectedSharedObjectRepository extends GeonetRepository<RejectedSharedObject, Integer>,
        JpaSpecificationExecutor<RejectedSharedObject> {
}
