package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * An entity representing a schematron description.
 * 
 * @author delawen
 */
@Entity
@Table(name = "schematrondes")
@Cacheable(value = true)
@Access(AccessType.PROPERTY)
public class SchematronDes extends GeonetEntity {

	private int id;
	private String description;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	public int getId() {
		return id;
	}

	public SchematronDes setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return "SchematronDes [_id=" + id + ", description=" + description
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SchematronDes other = (SchematronDes) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @return the schema
	 */
	@Column(nullable = false, name = "description")
	public String getDescription() {
		return description;
	}

	/**
	 * @param schema
	 *            the schema to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
