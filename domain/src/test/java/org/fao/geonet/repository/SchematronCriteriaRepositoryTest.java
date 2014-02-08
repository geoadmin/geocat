package org.fao.geonet.repository;

import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest.*;
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
    @Autowired
    private SchematronCriteriaGroupRepository criteriaGroupRepository;
    private AtomicInteger _inc = new AtomicInteger(10);

    @Test
    public void testFindOne() throws Exception {
        final SchematronCriteriaGroup criteriaGroup = criteriaGroupRepository.save(newGroup(_inc,_schematronRepo));

        final SchematronCriteria criteria = criteriaGroup.getCriteria().get(0);
        final SchematronCriteria found = _repo.findOne(criteria.getId());

        SpringDataTestSupport.assertSameContents(criteria, found);
    }


}
