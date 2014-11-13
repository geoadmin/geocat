package org.fao.geonet.repository.geocat.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.geocat.RejectedSharedObject;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for
 * User: Jesse
 * Date: 11/15/13
 * Time: 8:55 AM
 */
public class RejectedSharedObjectSpecificationsTest extends AbstractSpringDataTest {
    @Autowired
    private RejectedSharedObjectRepository _repo;

    @Test
    public void testHasId() throws Exception {
        final RejectedSharedObject obj1 = _repo.save(createRejectedObj());
        final RejectedSharedObject obj2 = _repo.save(createRejectedObj());
        final RejectedSharedObject obj3 = _repo.save(createRejectedObj());

        final List<RejectedSharedObject> all = _repo.findAll(RejectedSharedObjectSpecifications.hasId(obj1.getId(), obj2.getId()));

        assertEquals(2, all.size());

        List<Object> ids = Lists.transform(all, new Function<RejectedSharedObject, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable RejectedSharedObject input) {
                return input.getId();
            }
        });

        assertTrue(ids.contains(obj1.getId()));
        assertTrue(ids.contains(obj2.getId()));
        assertFalse(ids.contains(obj3.getId()));
    }

    private RejectedSharedObject createRejectedObj() {
        return new RejectedSharedObject().setXml("<d>data</d>");
    }
}
