package org.fao.geonet.domain.geocat;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.ISODate;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.Date;

/**
 * Represents a record of a metadata that was published or unpublished.
 *
 * User: Jesse
 * Date: 11/15/13
 * Time: 3:42 PM
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="publish_tracking")
public class PublishRecord extends GeonetEntity {
    private int id;
    private String uuid;
    private String entity;
    private char jpaWorkaround_Validated;
    private char jpaWorkaround_Published;
    private String failurerule;
    private String failurereasons;
    private Date changetime;
    private Date changedate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public PublishRecord setId(int id) {
        this.id = id;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public PublishRecord setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getEntity() {
        return entity;
    }

    public PublishRecord setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    @Transient
    public boolean isValidated() {
        return Constants.toBoolean_fromYNChar(getJpaWorkaround_Validated());
    }

    public PublishRecord setValidated(boolean validated) {
        setJpaWorkaround_Validated(Constants.toYN_EnabledChar(validated));
        return this;
    }
    protected char getJpaWorkaround_Validated() {
        return jpaWorkaround_Validated;
    }

    protected void setJpaWorkaround_Validated(char jpaWorkaround_Validated) {
        this.jpaWorkaround_Validated = jpaWorkaround_Validated;
    }

    @Transient
    public boolean isPublished() {
        return Constants.toBoolean_fromYNChar(getJpaWorkaround_Published());
    }

    public PublishRecord setPublished(boolean published) {
        setJpaWorkaround_Published(Constants.toYN_EnabledChar(published));
        return this;
    }

    protected char getJpaWorkaround_Published() {
        return jpaWorkaround_Published;
    }

    protected void setJpaWorkaround_Published(char jpaWorkaround_Published) {
        this.jpaWorkaround_Published = jpaWorkaround_Published;
    }

    public String getFailurerule() {
        return failurerule;
    }

    public PublishRecord setFailurerule(String failurerule) {
        this.failurerule = failurerule;
        return this;
    }

    public String getFailurereasons() {
        return failurereasons;
    }

    public PublishRecord setFailurereasons(String failurereasons) {
        this.failurereasons = failurereasons;
        return this;
    }

    @Temporal(TemporalType.DATE)
    public Date getChangetime() {
        return changetime;
    }

    public PublishRecord setChangetime(Date changetime) {
        this.changetime = changetime;
        return this;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getChangedate() {
        return changedate;
    }

    public PublishRecord setChangedate(Date changedate) {
        this.changedate = changedate;
        return this;
    }

    @Nonnull
    @Override
    public Element asXml() {
        return new Element("record")
                .addContent(new Element("uuid").setText("" + uuid))
                .addContent(new Element("entity").setText(entity))
                .addContent(new Element("validated").setText("" + isValidated()))
                .addContent(new Element("published").setText("" + isPublished()))
                .addContent(new Element("changedate").setText(new ISODate(changedate.getTime(), true).getDateAsString()))
                .addContent(new Element("changetime").setText(new ISODate(changetime.getTime(), false).getDateAndTime()))
                .addContent(new Element("failurerule").setText(failurerule))
                .addContent(new Element("failurereasons").setText(failurereasons));
    }
}
