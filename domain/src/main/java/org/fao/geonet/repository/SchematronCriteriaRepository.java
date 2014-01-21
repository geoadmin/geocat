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
		JpaSpecificationExecutor<SchematronCriteria>, SchematronCriteriaRepositoryCustom {

    /**
     * Get the list of all {@link org.fao.geonet.domain.SchematronCriteria} that are related to the given schematron.
     *
     * @param schematron the schematron in question
     *
     * @return the list of related schematron
     */
    List<SchematronCriteria> findAllBySchematron(Schematron schematron);
}
