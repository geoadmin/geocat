package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * An entity representing a schematron criteria. This is for the extended
 * validation framework ({@link Schematron}).
 * 
 * @author delawen
 */
@Entity
@Table(name = "schematroncriteria")
@Cacheable
@Access(AccessType.PROPERTY)
public class SchematronCriteria extends GeonetEntity {

	private int id;
	@Enumerated(EnumType.STRING)
	private SchematronCriteriaType type;
	private String value;
	private Schematron schematron;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	public int getId() {
		return id;
	}

	public SchematronCriteria setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return "SchematronCriteria [id=" + id + ", type=" + type
				+ ", value=" + value + ", schematron=" + schematron + "]";
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
		SchematronCriteria other = (SchematronCriteria) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @return the type
	 */
	@Column(nullable = false, name = "type")
	public SchematronCriteriaType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SchematronCriteriaType type) {
		this.type = type;
	}

	/**
	 * @return the value
	 */
	@Column(nullable = false, name = "value")
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the schematron
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "schematron", nullable = false, updatable = false)
	public Schematron getSchematron() {
		return schematron;
	}

	/**
	 * @param schematron
	 *            the schematron to set
	 */
	public void setSchematron(Schematron schematron) {
		this.schematron = schematron;
	}
}
