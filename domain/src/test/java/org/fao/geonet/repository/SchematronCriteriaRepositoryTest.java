package org.fao.geonet.repository;

import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test SchematronCriteriaRepository.
 *
 * Created by Jesse on 1/21/14.
 */
public class SchematronCriteriaRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SchematronRepository _schematronRepo;
    @Autowired
    private SchematronCriteriaRepository _repo;
    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testFindOne() throws Exception {
        final SchematronCriteria criteria = _repo.save(newSchematronCriteria(_schematronRepo, _inc));
        _repo.save(newSchematronCriteria(_schematronRepo, _inc));

        final SchematronCriteria found = _repo.findOne(criteria.getId());

        SpringDataTestSupport.assertSameContents(criteria, found);
    }
    @Test
    public void testFindAllBySchematron() throws Exception {
        final SchematronCriteria criteria = _repo.save(newSchematronCriteria(_schematronRepo, _inc));
        final SchematronCriteria criteria1 = _repo.save(newSchematronCriteria(_schematronRepo, _inc));
        final SchematronCriteria entity = newSchematronCriteria(_schematronRepo, _inc);
        entity.setSchematron(criteria.getSchematron());
        final SchematronCriteria criteria2 = _repo.save(entity);

        final List<SchematronCriteria> allBySchematron = _repo.findAllBySchematron(criteria.getSchematron());

        assertEquals(2, allBySchematron.size());

        for (SchematronCriteria schematronCriteria : allBySchematron) {
            if (schematronCriteria.getId() == criteria1.getId()) {
                fail("SchematronCriteria 2 should not have been found.  SchematronCriteria found are: "+allBySchematron);
            } else if (schematronCriteria.getId() != criteria.getId() && schematronCriteria.getId() != criteria2.getId()) {
                fail("SchematronCriteria id was neither from SchematronCriteria 1 or 2: "+allBySchematron);
            }
        }
    }

    public static SchematronCriteria newSchematronCriteria(SchematronRepository schematronRepository, AtomicInteger _inc) {
        Schematron schematron = schematronRepository.save(SchematronRepositoryTest.newSchematron(_inc));
        int id = _inc.incrementAndGet();

        final SchematronCriteria criteria = new SchematronCriteria();
        criteria.setSchematron(schematron);
        final SchematronCriteriaType[] values = SchematronCriteriaType.values();
        criteria.setType(values[id % values.length]);
        criteria.setValue("value_"+id);

        return criteria;
    }
}
