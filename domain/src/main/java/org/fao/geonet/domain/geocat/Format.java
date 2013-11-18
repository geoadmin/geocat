package org.fao.geonet.domain.geocat;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.GeonetEntity;

import javax.persistence.*;

/**
 * A Format spec.
 * User: Jesse
 * Date: 11/15/13
 * Time: 2:43 PM
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="Formats")
public class Format extends GeonetEntity {
    private int id;
    private String name;
    private String version;
    private char _jpaWorkaround_validated;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public Format setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Format setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Format setVersion(String version) {
        this.version = version;
        return this;
    }

    @Transient
    public boolean isValidated() {
        return Constants.toBoolean_fromYNChar(getJpaWorkaround_validated());
    }

    public Format setValidated(boolean validated) {
        setJpaWorkaround_validated(Constants.toYN_EnabledChar(validated));
        return this;
    }

    @Column(name="validated")
    protected char getJpaWorkaround_validated() {
        return _jpaWorkaround_validated;
    }

    protected void setJpaWorkaround_validated(char jpaWorkaround_validated) {
        this._jpaWorkaround_validated = jpaWorkaround_validated;
    }

    public boolean match(Format other) {
        boolean sameName = name.equalsIgnoreCase(other.name);
        boolean sameVersion = version.equalsIgnoreCase(other.version);
        return sameName && sameVersion;
    }
}
