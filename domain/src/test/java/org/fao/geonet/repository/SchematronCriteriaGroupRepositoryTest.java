package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.*;
import static org.junit.Assert.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test schematron group data access.
 *
 * Created by Jesse on 2/6/14.
 */
public class SchematronCriteriaGroupRepositoryTest extends AbstractSpringDataTest {


    private static final String GROUP_NAME_PREFIX = "GroupName_";
    @Autowired
    private SchematronRepository schematronRepository;
    @Autowired
    private SchematronCriteriaGroupRepository criteriaGroupRepository;

    @Test
     public void testFindAllBySchematron_schemaName() throws Exception {
        final SchematronCriteriaGroup g1 = criteriaGroupRepository.save(newGroup(_inc, schematronRepository));
        final SchematronCriteriaGroup g2 = criteriaGroupRepository.save(newGroup(_inc, schematronRepository));
        final SchematronCriteriaGroup g3PreSchematron = newGroup(_inc, schematronRepository);
        g3PreSchematron.setSchematron(g1.getSchematron());
        final SchematronCriteriaGroup g3 = criteriaGroupRepository.save(g3PreSchematron);

        List<SchematronCriteriaGroup> found =
                criteriaGroupRepository.findAllBySchematron_schemaName(g1.getSchematron().getSchemaName());

        List<String> foundIds = Lists.transform(found, new SchematronCriteriaGroupStringFunction());

        assertEquals(2, found.size());
        assertTrue(foundIds.contains(g1.getName()));
        assertTrue(foundIds.contains(g3.getName()));

        found = criteriaGroupRepository.findAllBySchematron_schemaName(g2.getSchematron().getSchemaName());

        foundIds = Lists.transform(found, new SchematronCriteriaGroupStringFunction());

        assertEquals(1, found.size());
        assertTrue(foundIds.contains(g2.getName()));


    }

    @Test
    public void testOne() throws Exception {
        final SchematronCriteriaGroup g1 = criteriaGroupRepository.save(newGroup(_inc, schematronRepository));
        final SchematronCriteriaGroup g2 = criteriaGroupRepository.save(newGroup(_inc, schematronRepository));

        final SchematronCriteriaGroup found1 = criteriaGroupRepository.findOne(g1.getName());
        assertSameContents(g1, found1);
        assertCorrectNumberOfCriteria(found1);
        final SchematronCriteriaGroup found2 = criteriaGroupRepository.findOne(g2.getName());
        assertSameContents(g2, found2);
        assertCorrectNumberOfCriteria(found2);

    }

    private void assertCorrectNumberOfCriteria(SchematronCriteriaGroup g1) {
        String id = g1.getName().substring(GROUP_NAME_PREFIX.length());
        assertEquals(Integer.parseInt(id), g1.getCriteria().size());
    }

    public static SchematronCriteriaGroup newGroup(AtomicInteger inc, SchematronRepository schematronRepository) {
        Schematron schematron = schematronRepository.save(SchematronRepositoryTest.newSchematron(inc));
        int id = inc.incrementAndGet();

        SchematronCriteriaGroup group = new SchematronCriteriaGroup();
        group.setName(GROUP_NAME_PREFIX + id);
        group.setSchematron(schematron);
        final SchematronRequirement[] requirements = SchematronRequirement.values();
        group.setRequirement(requirements[id % requirements.length]);
        for (int i = 0 ; i < id; i++) {
            group.addCriteria(newSchematronCriteria(inc));
        }

        return group;

    }

    public static SchematronCriteria newSchematronCriteria(AtomicInteger inc) {
        int id = inc.incrementAndGet();

        final SchematronCriteria criteria = new SchematronCriteria();
        final SchematronCriteriaType[] values = SchematronCriteriaType.values();
        criteria.setType(values[id % values.length]);
        criteria.setValue("value_"+id);

        return criteria;
    }

    private static class SchematronCriteriaGroupStringFunction implements Function<SchematronCriteriaGroup, String> {
        @Nullable
        @Override
        public String apply(@Nullable SchematronCriteriaGroup input) {
            return input.getName();
        }
    }
}
