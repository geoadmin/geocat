package org.fao.geonet.repository.geocat.specification;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.geocat.GeocatUserInfo_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Specs for querying users that are GeocatSpecific.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 12:20 PM
 */
public class GeocatUserSpecs {
    public static Specification<User> isValidated(final boolean validated) {
        return new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(User_.geocatUserInfo).get(GeocatUserInfo_.jpaWorkaround_validated), Constants.toYN_EnabledChar(validated));
            }
        };
    }

    public static Specification<User>  hasParentIdIn(final Collection<Integer> parentIds) {
        return  new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(User_.geocatUserInfo).get(GeocatUserInfo_.parentInfo).in(parentIds);
            }
        };
    }
}
