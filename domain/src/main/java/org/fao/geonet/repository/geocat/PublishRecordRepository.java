package org.fao.geonet.repository.geocat;

import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.repository.GeonetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for accessing {@link org.fao.geonet.domain.geocat.PublishRecord} objects.
 * User: Jesse
 * Date: 11/14/13
 * Time: 3:06 PM
 */
public interface PublishRecordRepository extends GeonetRepository<PublishRecord, Integer>,
        JpaSpecificationExecutor<PublishRecord> {
}
