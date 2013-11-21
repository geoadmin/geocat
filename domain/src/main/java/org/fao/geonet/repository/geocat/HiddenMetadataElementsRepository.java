package org.fao.geonet.repository.geocat;

import org.fao.geonet.domain.geocat.HiddenMetadataElement;
import org.fao.geonet.repository.GeonetRepository;

import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataCategory} entities.
 *
 * @author Jesse
 */
public interface HiddenMetadataElementsRepository extends GeonetRepository<HiddenMetadataElement, Integer> {
    public List<HiddenMetadataElement> findAllByMetadataId(int metadataId);
    public List<HiddenMetadataElement> findAllByMetadataIdAndXPathExpr(int metadataId, String xpathExpr);

}
