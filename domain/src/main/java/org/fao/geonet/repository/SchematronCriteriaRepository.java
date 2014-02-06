package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronCriteria;
import org.jdom.Element;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for the {@link SchematronCriteria} entities.
 * 
 * @author delawen
 */
public interface SchematronCriteriaRepository extends
		GeonetRepository<SchematronCriteria, Integer>,
		JpaSpecificationExecutor<SchematronCriteria> {

    /**
     * Get the list of all {@link org.fao.geonet.domain.SchematronCriteria} that are contained in the
     * {@link org.fao.geonet.domain.SchematronCriteriaGroup}
     *
     * @param schematronCriteriaGroupName the name of the in question {@link org.fao.geonet.domain.SchematronCriteriaGroup}
     *
     * @return the list of related schematron criteria
     */
    List<SchematronCriteria> findAllByGroup_Name(String schematronCriteriaGroupName);
}
