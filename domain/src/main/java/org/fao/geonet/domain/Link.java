/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import org.fao.geonet.domain.converter.LinkTypeConverter;
import org.fao.geonet.entitylistener.LinkEntityListenerManager;
import org.hibernate.annotations.Type;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * An entity representing link. A link can be a URL in a metadata record.
 */
@Entity
@Table(name = "Links")
@Cacheable
@Access(AccessType.PROPERTY)
@EntityListeners(LinkEntityListenerManager.class)
@SequenceGenerator(name = Link.ID_SEQ_NAME, initialValue = 1, allocationSize = 1)
public class Link implements Serializable {
    static final String ID_SEQ_NAME = "link_id_seq";

    private int _id;
    private String _url;
    private String _protocol;
    private LinkType _linkType = LinkType.HTTP;
    private Set<MetadataLink> records = new HashSet<>();
    private Set<LinkStatus> linkStatus = new TreeSet<>(LAST_STATUS_COMPARATOR);
    private Integer lastState = 0;

    private static final Comparator<? super LinkStatus> LAST_STATUS_COMPARATOR = new Comparator<LinkStatus>() {
        @Override
        public int compare(LinkStatus left, LinkStatus right) {
            return right.getcheckDate().compareTo(left.getcheckDate());
        }
    };

    /**
     * Get the id of the link.
     * <p>
     * This is autogenerated and when a new link is created the link will be
     * assigned a new value.
     *
     * @return the id of the link.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    public Link setId(int id) {
        this._id = id;
        return this;
    }


    /**
     * Link type is used to classify the link and find the proper
     * link checker to use. For now, only HTTP(s) are supported.
     */
    @Column(nullable = false)
    @Convert(converter = LinkTypeConverter.class)
    public LinkType getLinkType() {
        return _linkType;
    }

    public Link setLinkType(LinkType _linkType) {
        this._linkType = _linkType;
        return this;
    }

    /**
     * Protocol depends on metadata standards and categorize URL in
     * more precise types. This information can be used to do additional
     * checks depending on the protocol (eg. for a WMS service, the GetCapabilities
     * request can be checked, for a WMS layer, the presence of the layer in the
     * GetCapabilities can be checked). It is not used for now.
     */
    @Column(nullable = true)
    public String getProtocol() {
        return _protocol;
    }

    public Link setProtocol(String protocol) {
        this._protocol = protocol;
        return this;
    }

    /**
     * Get all status information for a link.
     *
     * @return
     */
    @OneToMany(cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        mappedBy = "linkId",
        orphanRemoval = true)
    public Set<LinkStatus> getLinkStatus() {
        return linkStatus;
    }

    public Link setLinkStatus(Set<LinkStatus> linkStatus) {
        this.linkStatus = linkStatus;
        return this;
    }

    public Integer getLastState() {
        synchronizeLastState();
        return lastState;
    }

    public void setLastState(Integer lastState) {
        synchronizeLastState();
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
        mappedBy = "link",
        orphanRemoval = true)
    public Set<MetadataLink> getRecords() {
        return records;
    }

    public Link setRecords(Set<MetadataLink> records) {
        this.records = records;
        return this;
    }


    /**
     * Get the URL of the link. Usually a HTTP(s) URL.
     *
     * @return URL
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(unique = true)
    public String getUrl() {
        return _url;
    }

    public Link setUrl(String url) {
        this._url = url;
        return this;
    }

    private void synchronizeLastState() {
        if (linkStatus.size() > 0) {
            this.lastState = convertStatusToState(linkStatus.stream().findFirst().get());
        }
    }

    private Integer convertStatusToState(LinkStatus lastStatus) {
        if (lastStatus == null) {
            return 0;
        }
        if (lastStatus.isFailing()) {
            return -1;
        }
        return 1;
    }
}
