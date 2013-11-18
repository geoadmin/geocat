package org.fao.geonet.repository.geocat.specification;

import org.fao.geonet.domain.geocat.RejectedSharedObject;
import org.fao.geonet.domain.geocat.RejectedSharedObject_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Specifications for {@link RejectedSharedObject}
 * User: Jesse
 * Date: 11/15/13
 * Time: 8:45 AM
 */
public class RejectedSharedObjectSpecifications {
    public static Specification<RejectedSharedObject> hasId(final Integer... ids) {
        return new Specification<RejectedSharedObject>() {
            @Override
            public Predicate toPredicate(Root<RejectedSharedObject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(RejectedSharedObject_.id).in(ids);
            }
        };
    }
}
