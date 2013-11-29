package org.fao.geonet.domain;

import javax.persistence.*;

/**
 * An entity representing a schematron. It contains the file to the schematron
 * definition, the schema it belongs and if it is required or just a
 * recommendation.
 * 
 * @author delawen
 */
@Entity
@Table(name = "schematron")
@Cacheable
@Access(AccessType.PROPERTY)
@SequenceGenerator(name=Schematron.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class Schematron extends Localized {
    static final String ID_SEQ_NAME = "schematron_id_seq";

	private int id;
	private String isoschema;
	private String file;
	private Boolean required;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
	@Column(nullable = false)
	public int getId() {
		return id;
	}

	public Schematron setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return "Schematron [_id=" + id + ", isoschema=" + isoschema + ", file="
				+ file + ", required=" + required + ", description"
				+ getLabelTranslations() + "]";
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
		Schematron other = (Schematron) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @return the schema
	 */
	@Column(nullable = false, name = "isoschema")
	public String getIsoschema() {
		return isoschema;
	}

	/**
	 * @param schema
	 *            the schema to set
	 */
	public void setIsoschema(String schema) {
		this.isoschema = schema;
	}

	/**
	 * @return the file
	 */
	@Column(nullable = false, name = "file")
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @return the required
	 */
	@Column(nullable = false, name = "required")
	public Boolean getRequired() {
		return required;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

}
