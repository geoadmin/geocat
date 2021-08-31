package org.fao.geonet.domain.geocat;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.ISODate;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.Date;
import java.util.IdentityHashMap;

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
@SequenceGenerator(name= PublishRecord.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class PublishRecord extends GeonetEntity {
    static final String ID_SEQ_NAME = "publish_record_id_seq";
    private Integer groupOwner;
    private String source;
    private int id;
    private String uuid;
    private String entity;
    private char jpaWorkaround_Validated;
    private char jpaWorkaround_Published;
    private String failurerule;
    private String failurereasons;
    private Date changetime = new Date();
    private Date changedate = new Date();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
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

    /**
     * Get the group the metadata belongs to.
     */
    public Integer getGroupOwner() {
        return groupOwner;
    }

    /**
     * Set the group the metadata belongs to.
     */
    public void setGroupOwner(Integer groupOwner) {
        this.groupOwner = groupOwner;
    }

    /**
     * Get the source instance of the metadata that has been unpublished or published
     */
    public String getSource() {
        return source;
    }

    /**
     * Set the source instance of the metadata that has been unpublished or published
     */
    public void setSource(String source) {
        this.source = source;
    }

    @Transient
    public Validity isValidated() {
        return Validity.parse(getJpaWorkaround_Validated());
    }

    public PublishRecord setValidated(Validity validated) {
        setJpaWorkaround_Validated(validated.dbCode);
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

    @Temporal(TemporalType.TIMESTAMP)
    public Date getChangetime() {
        return cloneDate(changetime);
    }

    public PublishRecord setChangetime(Date changetime) {
        this.changetime = cloneDate(changetime);
        return this;
    }

    @Temporal(TemporalType.DATE)
    public Date getChangedate() {
        return cloneDate(changedate);
    }

    public PublishRecord setChangedate(Date changedate) {
        this.changedate = cloneDate(changedate);
        return this;
    }

    private Date cloneDate(Date changedate) {
        if (changedate == null) {
            return null;
        } else {
            return (Date) changedate.clone();
        }
    }

    @Nonnull
    @Override
    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        Element el = new Element("record")
                .addContent(new Element("uuid").setText("" + uuid))
                .addContent(new Element("entity").setText(entity))
                .addContent(new Element("validated").setText("" + isValidated()))
                .addContent(new Element("published").setText("" + isPublished()))
                .addContent(new Element("changedate").setText(new ISODate(changedate.getTime(), true).getDateAsString()))
                .addContent(new Element("changetime").setText(new ISODate(changetime.getTime(), false).getDateAndTime()))
                .addContent(new Element("failurerule").setText(failurerule))
                .addContent(new Element("failurereasons").setText(failurereasons));

        if (groupOwner != null) {
            el.addContent(new Element("groupOwner").setText(groupOwner.toString()));
        }

        if (source != null) {
            el.addContent(new Element("source").setText(source));
        }
        return el;
    }

    public static enum Validity {
        VALID('y'), INVALID('n'), UNKNOWN('?');

        final char dbCode;

        private Validity(char dbCode) {
            this.dbCode = dbCode;
        }

        public static Validity fromBoolean(boolean validated) {
            return validated ? VALID : INVALID;
        }

        public static Validity parse(char code) {
            for (Validity v : values()) {
                if(v.dbCode == code) return v;
            }
            return UNKNOWN;
        }
    }}
