package org.fao.geonet.domain.geocat;

import org.fao.geonet.domain.Constants;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Geocat specific information associated with the user.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 11:04 AM
 */
@Embeddable
public class GeocatUserInfo implements Serializable {
    private Integer _parentInfo;
    private char _jpaWorkaround_validated;
    private String positionname;
    private String onlineresource;
    private String hoursofservice;
    private String contactinstructions;
    private String publicaccess;
    private String orgacronym;
    private String onlinename;
    private String onlinedescription;

    @Transient
    public boolean isValidated() {
        return Constants.toBoolean_fromYNChar(getJpaWorkaround_validated());
    }

    public GeocatUserInfo setValidated(boolean validated) {
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

    public Integer getParentInfo() {
        return _parentInfo;
    }

    public GeocatUserInfo setParentInfo(Integer parentInfo) {
        this._parentInfo = parentInfo;
        return this;
    }

    public String getPositionname() {
        return positionname;
    }

    public GeocatUserInfo setPositionname(String positionname) {
        this.positionname = positionname;
        return this;
    }

    public String getOnlineresource() {
        return onlineresource;
    }

    public GeocatUserInfo setOnlineresource(String onlineresource) {
        this.onlineresource = onlineresource;
        return this;
    }

    public String getHoursofservice() {
        return hoursofservice;
    }

    public GeocatUserInfo setHoursofservice(String hoursofservice) {
        this.hoursofservice = hoursofservice;
        return this;
    }

    public String getContactinstructions() {
        return contactinstructions;
    }

    public GeocatUserInfo setContactinstructions(String contactinstructions) {
        this.contactinstructions = contactinstructions;
        return this;
    }

    public String getPublicaccess() {
        return publicaccess;
    }

    public GeocatUserInfo setPublicaccess(String publicaccess) {
        this.publicaccess = publicaccess;
        return this;
    }

    public String getOrgacronym() {
        return orgacronym;
    }

    public GeocatUserInfo setOrgacronym(String orgacronym) {
        this.orgacronym = orgacronym;
        return this;
    }

    public String getOnlinename() {
        return onlinename;
    }

    public GeocatUserInfo setOnlinename(String onlinename) {
        this.onlinename = onlinename;
        return this;
    }

    public String getOnlinedescription() {
        return onlinedescription;
    }

    public GeocatUserInfo setOnlinedescription(String onlinedescription) {
        this.onlinedescription = onlinedescription;
        return this;
    }
}
