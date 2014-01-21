package org.fao.geonet.repository;

import static org.junit.Assert.*;

import org.fao.geonet.domain.Schematron;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test {@link org.fao.geonet.repository.SchematronRepository}
 * Created by Jesse on 1/21/14.
 */
public class SchematronRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SchematronRepository _repo;
    private AtomicInteger _inc = new AtomicInteger();


    @Test
    public void testFindOne() throws Exception {
        final Schematron schematron = _repo.save(newSchematron(_inc));

        final Schematron found = _repo.findOne(schematron.getId());

        SpringDataTestSupport.assertSameContents(schematron, found);
    }

    @Test
    public void testFindAllByIsoschema() throws Exception {
        final Schematron schematron1 = _repo.save(newSchematron(_inc));
        final Schematron schematron2 = _repo.save(newSchematron(_inc));
        final Schematron entity = newSchematron(_inc);
        entity.setIsoschema(schematron1.getIsoschema());
        final Schematron schematron3 = _repo.save(entity);

        final List<Schematron> allByIsoschema = _repo.findAllByIsoschema(schematron1.getIsoschema());

        assertEquals(2, allByIsoschema.size());

        for (Schematron schematron : allByIsoschema) {
            if (schematron.getId() == schematron2.getId()) {
                fail("schematron 2 should not have been found.  Schematron found are: "+allByIsoschema);
            } else if (schematron.getId() != schematron1.getId() && schematron.getId() != schematron3.getId()) {
                fail("schematron id was neither from schematron 1 or 2: "+allByIsoschema);
            }
        }
    }

    @Test
    public void testFindAllByFile() throws Exception {
        final Schematron schematron1 = _repo.save(newSchematron(_inc));
        final Schematron schematron2 = _repo.save(newSchematron(_inc));
        final Schematron entity = newSchematron(_inc);
        entity.setFile(schematron1.getFile());
        final Schematron schematron3 = _repo.save(entity);

        final List<Schematron> allByFile = _repo.findAllByFile(schematron1.getFile());

        assertEquals(2, allByFile.size());

        for (Schematron schematron : allByFile) {
            if (schematron.getId() == schematron2.getId()) {
                fail("schematron 2 should not have been found.  Schematron found are: "+allByFile);
            } else if (schematron.getId() != schematron1.getId() && schematron.getId() != schematron3.getId()) {
                fail("schematron id was neither from schematron 1 or 2: "+allByFile);
            }
        }

    }

    public static Schematron newSchematron(AtomicInteger inc) {
        int id = inc.incrementAndGet();

        final Schematron schematron = new Schematron();
        schematron.setFile("file"+id);
        schematron.setIsoschema("schema"+id);
        schematron.setRequired(id % 2 == 0);

        return schematron;
    }
}
