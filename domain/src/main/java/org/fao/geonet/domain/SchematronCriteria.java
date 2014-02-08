package org.fao.geonet.domain;

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.List;

/**
 * An entity representing a schematron criteria. This is for the extended
 * validation framework ({@link Schematron}).
 * 
 * @author delawen
 */
@Entity
@Table(name = "SchematronCriteria")
@Cacheable
@Access(AccessType.PROPERTY)
@SequenceGenerator(name=SchematronCriteria.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class SchematronCriteria extends GeonetEntity {
    static final String ID_SEQ_NAME = "schematron_criteria_id_seq";

	private int id;
	private SchematronCriteriaType type;
	private String value;
    private SchematronCriteriaGroup group;

    /**
     * Get the unique id for the schematron criteria object
     */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
	@Column(nullable = false)
	public int getId() {
		return id;
	}

    /**
     * Set the unique id for the schematron criteria object
     */
	public SchematronCriteria setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return "SchematronCriteria [id=" + id + ", type=" + type
				+ ", value=" + value + "]";
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
    @Enumerated(EnumType.STRING)
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
     * Get the group this schematron criteria is part of.
     *
     * @return the containing group
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "name")
    public SchematronCriteriaGroup getGroup() {
        return group;
    }

    /**
     * Set the group for this criteria.
     *
     * @param group the group to contain this criteria
     */
    public void setGroup(SchematronCriteriaGroup group) {
        this.group = group;
    }

    public boolean accepts(ApplicationContext applicationContext, Element metadata, List<Namespace> metadataNamespaces) {
        return getType().accepts(applicationContext, getValue(), metadata, metadataNamespaces);
    }

    @Nonnull
    @Override
    public Element asXml() {
        return super.asXml();
    }
}
