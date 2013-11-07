package org.fao.geonet.repository;

import java.util.List;

import org.fao.geonet.domain.SchematronCriteria;
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
	 * Look up a schematrons by its schema
	 * 
	 * @param name
	 *            the name of the schematron
	 */
	public List<SchematronCriteria> findAll();

}
