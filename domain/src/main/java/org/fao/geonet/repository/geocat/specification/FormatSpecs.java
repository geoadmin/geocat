package org.fao.geonet.repository.geocat.specification;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.domain.geocat.Format_;
import org.fao.geonet.domain.geocat.GeocatUserInfo_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Format specs
 * User: Jesse
 * Date: 11/15/13
 * Time: 3:14 PM
 */
public class FormatSpecs {
    public static Specification<Format> isValidated(final boolean validated) {
        return new Specification<Format>() {
            @Override
            public Predicate toPredicate(Root<Format> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.get(Format_.jpaWorkaround_validated), Constants.toYN_EnabledChar(validated));
            }
        };
    }

    public static Specification<Format> hasIdIn(final Collection<Integer> ids) {
        return new Specification<Format>() {
            @Override
            public Predicate toPredicate(Root<Format> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(Format_.id).in(ids);
            }
        };
    }

}
