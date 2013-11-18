package org.fao.geonet.repository.geocat.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.UserRepositoryTest.newUser;
import static org.fao.geonet.repository.specification.UserSpecs.hasUserIdIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test Geocat specific user specs.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 2:02 PM
 */
public class GeocatUserSpecsTest extends AbstractSpringDataTest {
    @Autowired
    UserRepository _userRepo;
    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testIsValidated() throws Exception {
        User user1 = newUser(_inc);
        user1.getGeocatUserInfo().setValidated(true);
        user1 = _userRepo.save(user1);

        User user2 = newUser(_inc);
        user2.getGeocatUserInfo().setValidated(true);
        user2 = _userRepo.save(user2);

        User user3 = newUser(_inc);
        user3.getGeocatUserInfo().setValidated(false);
        user3 = _userRepo.save(user3);

        final List<User> found = _userRepo.findAll(GeocatUserSpecs.isValidated(true));

        assertEquals(2, found.size());
        List<Integer> foundIds = Lists.transform(found, new Function<User, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable User input) {
                return input.getId();
            }
        });

        assertTrue(foundIds.contains(user1.getId()));
        assertTrue(foundIds.contains(user2.getId()));
        assertFalse(foundIds.contains(user3.getId()));
    }

    @Test
    public void testHasParentIdIn() throws Exception {
        User user1 = newUser(_inc);
        user1 = _userRepo.save(user1);

        User user2 = newUser(_inc);
        user2.getGeocatUserInfo().setParentInfo(user1.getId());
        user2 = _userRepo.save(user2);

        List<User> found = _userRepo.findAll(GeocatUserSpecs.hasParentIdIn(Arrays.asList(user1.getId())));

        assertEquals(1, found.size());
        assertEquals(user2.getId(), found.get(0).getId());

        found = _userRepo.findAll(GeocatUserSpecs.hasParentIdIn(Arrays.asList(user2.getId())));

        assertEquals(0, found.size());

    }
}
