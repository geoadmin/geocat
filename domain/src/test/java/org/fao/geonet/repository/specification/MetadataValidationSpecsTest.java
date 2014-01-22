package org.fao.geonet.repository.specification;

import static org.junit.Assert.*;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test metadata validation specification class.
 *
 * Created by Jesse on 1/21/14.
 */
public class MetadataValidationSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRepository _metadataRepo;
    @Autowired
    MetadataValidationRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testHasMetadataId() throws Exception {
        final MetadataValidation validation = _repo.save(MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepo));
        final MetadataValidation validation2 = _repo.save(MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepo));

        final Specification<MetadataValidation> hasMetadata1Id = MetadataValidationSpecs.hasMetadataId(validation.getId().getMetadataId());
        assertEquals(1, _repo.count(hasMetadata1Id));
        final Specification<MetadataValidation> hasMetadata2Id = MetadataValidationSpecs.hasMetadataId(validation2.getId().getMetadataId());
        assertEquals(1, _repo.count(hasMetadata2Id));

        SpringDataTestSupport.assertSameContents(validation, _repo.findOne(hasMetadata1Id));
        SpringDataTestSupport.assertSameContents(validation2, _repo.findOne(hasMetadata2Id));
    }
}
