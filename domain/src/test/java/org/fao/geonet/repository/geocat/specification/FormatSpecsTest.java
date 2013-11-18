package org.fao.geonet.repository.geocat.specification;

import static org.junit.Assert.*;

import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.geocat.FormatRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Test format specs.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 3:16 PM
 */
public class FormatSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private FormatRepository _formatRepo;

    @Test
    public void testIsValidated() throws Exception {
        final Format format = createFormat(1, false);
        Format saved = _formatRepo.save(format);

        Format found = _formatRepo.findOne(FormatSpecs.isValidated(false));

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());

        assertNull (_formatRepo.findOne(FormatSpecs.isValidated(true)));

        format.setValidated(true);
        _formatRepo.save(format);

        assertNull (_formatRepo.findOne(FormatSpecs.isValidated(false)));
        assertNotNull(_formatRepo.findOne(FormatSpecs.isValidated(true)));
    }


    @Test
    public void testHasIdIn() throws Exception {
        final Format format = createFormat(1, false);
        Format saved = _formatRepo.save(format);

        Format found = _formatRepo.findOne(FormatSpecs.hasIdIn(Arrays.asList(saved.getId())));

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());

        found = _formatRepo.findOne(FormatSpecs.hasIdIn(Arrays.asList(saved.getId() + 1)));

        assertNull(found);
    }

    private Format createFormat(int id, boolean validated) {
        final Format format = new Format();
        format.setName("name"+id);
        format.setVersion("version" + id);
        format.setValidated(validated);
        return format;
    }
}
