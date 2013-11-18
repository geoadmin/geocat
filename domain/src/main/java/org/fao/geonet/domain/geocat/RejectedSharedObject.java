package org.fao.geonet.domain.geocat;

import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.IOException;

/**
 * Represents a rejected shared object.
 *
 * User: Jesse
 * Date: 11/14/13
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "DeletedObjects")
public class RejectedSharedObject extends GeonetEntity {

    private static final Namespace XLINK = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
    private int id;
    private String description;
    private String xml;
    private ISODate deletionDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public ISODate getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(ISODate deletionDate) {
        this.deletionDate = deletionDate;
    }

    public Element getXmlElement(boolean validate) throws IOException, JDOMException {
        final Element obj = Xml.loadString(getXml(), false);
        obj.removeAttribute("title", XLINK);
        obj.removeNamespaceDeclaration(XLINK);
        return obj;
    }
}
