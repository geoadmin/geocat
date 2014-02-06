package org.fao.geonet.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesse on 2/6/14.
 */
@Entity
@Table(name = "SchematronCriteriaGroup")
@Cacheable
@Access(AccessType.PROPERTY)
public class SchematronCriteriaGroup extends GeonetEntity {
    private String name;
    private List<SchematronCriteria> criteriaList = new ArrayList<SchematronCriteria>();
    @Enumerated(EnumType.STRING)
    private SchematronRequirement requirement;
    private Schematron schematron;

    /**
     * Get the name/id of this group. This is only shown to the administrator
     */
    @Id
    public String getName() {
        return name;
    }

    /**
     * Set the name/id of this group.
     *
     * @param name the group name/id
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the schematron criteria that of this group.
     *
     * @return the schematron criteria that of this group.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "group", orphanRemoval = true)
    public List<SchematronCriteria> getCriteriaList() {
        return criteriaList;
    }

    /**
     * Set the schematron criteria that of this group.
     * <p/>
     * Use {@link #addCriteria(SchematronCriteria)} for adding criteria to this group rather than adding
     * the criteria to this list.
     * @param criteriaList the schematron criteria that of this group.
     */
    public void setCriteriaList(List<SchematronCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    /**
     * Get the requirement value if this criteria group is applicable for the metadata.
     *
     * @return the requirement.
     */
    @Column(nullable = false)
    public SchematronRequirement getRequirement() {
        return requirement;
    }

    /**
     * Set the level requirement for this group.
     *
     *
     * @param requirement
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setRequirement(SchematronRequirement requirement) {
        this.requirement = requirement;
        return this;
    }
    /**
     * Get the schematron this group applies to.
     *
     * @return the schematron
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "schematron", nullable = false, updatable = false)
    public Schematron getSchematron() {
        return schematron;
    }

    /**
     * Set the schematron this group applies to.
     *
     *
     * @param schematron
     *            the schematron to set
     *
     * @return this entity
     */
    public SchematronCriteriaGroup setSchematron(Schematron schematron) {
        this.schematron = schematron;
        return this;
    }

    /**
     * Set the group on the criteria object and add to the list of criteria.
     *
     * @param schematronCriteria the criteria to add to this group.
     *
     * @return this entity
     */
    public SchematronCriteriaGroup addCriteria(SchematronCriteria schematronCriteria) {
        schematronCriteria.setGroup(this);
        getCriteriaList().add(schematronCriteria);
        return this;
    }
}
