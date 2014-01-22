package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId_;
import org.fao.geonet.domain.MetadataValidation_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Contain specifications for searching through MetadataValidationRepository.
 *
 * Created by Jesse on 1/21/14.
 */
public class MetadataValidationSpecs {

    /**
     * A specifiction that selects the {@link org.fao.geonet.domain.MetadataValidation} entities with the give metadataId.
     *
     * @param metadataId the metadataId to use for the selection.
     */
    public static Specification<MetadataValidation> hasMetadataId(final int metadataId) {
        return new Specification<MetadataValidation>() {
            @Override
            public Predicate toPredicate(Root<MetadataValidation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(MetadataValidation_.id).get(MetadataValidationId_.metadataId), metadataId);
            }
        };
    }
}
