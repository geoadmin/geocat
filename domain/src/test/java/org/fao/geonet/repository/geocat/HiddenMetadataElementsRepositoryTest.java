package org.fao.geonet.repository.geocat;

import static org.junit.Assert.*;

import org.fao.geonet.domain.geocat.HiddenMetadataElement;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SpringDataTestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Test HiddenMetadataElementsRepository.
 *
 * User: Jesse
 * Date: 11/8/13
 * Time: 4:09 PM
 */
public class HiddenMetadataElementsRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    HiddenMetadataElementsRepository repo;

    @Test
    public void testFindOne() throws Exception {
        HiddenMetadataElement elem = new HiddenMetadataElement();
        elem.setLevel("intranet");
        elem.setxPathExpr("someXPath");
        elem.setMetadataId(1);
        repo.save(elem);

        HiddenMetadataElement found = repo.findOne(elem.getxPathExpr());
        SpringDataTestSupport.assertSameContents(elem, found);
    }

    @Test
    public void testFindAllByMetadataId() throws Exception {
        HiddenMetadataElement elem = new HiddenMetadataElement();
        elem.setLevel("all");
        elem.setxPathExpr("someXPath");
        elem.setMetadataId(1);
        repo.save(elem);

        HiddenMetadataElement elem2 = new HiddenMetadataElement();
        elem2.setLevel("intranet");
        elem2.setxPathExpr("someXPath2");
        elem2.setMetadataId(2);
        repo.save(elem2);

        List<HiddenMetadataElement> found = repo.findAllByMetadataId(2);
        assertEquals(1, found.size());
        SpringDataTestSupport.assertSameContents(elem2, found.get(0));

        found = repo.findAllByMetadataId(1);
        assertEquals(1, found.size());
        SpringDataTestSupport.assertSameContents(elem, found.get(0));
    }
}
