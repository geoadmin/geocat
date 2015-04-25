package org.fao.geonet.domain.geocat;

import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Jesse on 3/16/2015.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="geom_table_lastmodified")
public class GeomTableLastModified {
    private String name;
    private Date lastmodified;

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastmodified() {
        return (Date) lastmodified.clone();
    }

    public void setLastmodified(Date lastmodified) {
        this.lastmodified = (Date) lastmodified.clone();
    }
}
