package org.fao.geonet.domain.geocat;

import javax.persistence.*;

/**
 * An entity for the hidden elements of Geocat.
 *
 * User: Jesse
 * Date: 11/8/13
 * Time: 4:03 PM
 */
@Entity
@Access(AccessType.PROPERTY)
@Table (name="HiddenMetadataElements")
@SequenceGenerator(name=HiddenMetadataElement.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class HiddenMetadataElement {
    static final String ID_SEQ_NAME = "hidden_metadata_element_id_seq";
    int id;
    String _xPathExpr;
    String _level;
    int _metadataId;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getxPathExpr() {
        return _xPathExpr;
    }

    /**
     * Set xpath of the hidden element.
     *
     * @param xPathExpr xpath of the hidden element.
     */
    public void setxPathExpr(String xPathExpr) {
        this._xPathExpr = xPathExpr;
    }

    /**
     * Get the index of the element to be hidden.
     *
     * @return the index of the element to be hidden.
     */
    public String getLevel() {
        return _level;
    }

    /**
     * Set the index of the element to be hidden.
     * @param level the index of the element to be hidden.
     */
    public void setLevel(String level) {
        this._level = level;
    }

    /**
     * Return the metadata id this element is associated with.
     *
     * @return the metadata id this element is associated with.
     */
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the metadata id this element is associated with.
     *
     * @param metadataId the ids
     */
    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }
}
