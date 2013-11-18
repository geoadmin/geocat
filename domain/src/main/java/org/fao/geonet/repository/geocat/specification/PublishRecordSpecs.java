package org.fao.geonet.repository.geocat.specification;

import org.fao.geonet.domain.geocat.PublishRecord;
import org.fao.geonet.domain.geocat.PublishRecord_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;

/**
 * Format specs
 * User: Jesse
 * Date: 11/15/13
 * Time: 3:14 PM
 */
public class PublishRecordSpecs {
    public static Specification<PublishRecord> newerThanDate(final Date date) {
        return new Specification<PublishRecord>() {
            @Override
            public Predicate toPredicate(Root<PublishRecord> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.greaterThan(root.get(PublishRecord_.changedate), date);
            }
        };
    }
    public static Specification<PublishRecord> olderThanOrEqualToDate(final Date date) {
        return new Specification<PublishRecord>() {
            @Override
            public Predicate toPredicate(Root<PublishRecord> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.lessThanOrEqualTo(root.get(PublishRecord_.changedate), date);
            }
        };
    }

    public static Specification<PublishRecord> daysOldOrNewer(final int days) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1 * days);
        return newerThanDate(cal.getTime());
    }

    public static Specification<PublishRecord> daysOldOrOlder(int days) {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1 * days);
        return olderThanOrEqualToDate(cal.getTime());
    }
}
