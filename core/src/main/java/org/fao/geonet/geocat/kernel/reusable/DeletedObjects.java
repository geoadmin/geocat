//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.geocat.RejectedSharedObject;
import org.fao.geonet.domain.geocat.RejectedSharedObject_;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.fao.geonet.repository.geocat.specification.RejectedSharedObjectSpecifications;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.util.List;

import static org.apache.lucene.search.WildcardQuery.WILDCARD_STRING;

/**
 * Handles operations for interacting with the Deleted Objects database table
 *
 * @author jeichar
 */
public final class DeletedObjects {

    public static int insert(RejectedSharedObjectRepository repo, String fragment, String desc, String rejectionMessage) throws SQLException {
        org.fao.geonet.domain.geocat.RejectedSharedObject entity = new org.fao.geonet.domain.geocat.RejectedSharedObject();
        entity.setDeletionDate(new ISODate());
        entity.setDescription(desc);
        entity.setXml(fragment);
        entity.setRejectionMessage(rejectionMessage);
        return repo.save(entity).getId();
    }

    public static String href(int id) {
        return "local://xml.reusable.deleted?id=" + id;
    }

    public static Element get(RejectedSharedObjectRepository repo, String id) throws Exception {
        RejectedSharedObject sharedObject = repo.findOne(Integer.parseInt(id));
        if (sharedObject != null) {
            return sharedObject.getXmlElement(false);
        } else {
            return new Element("ERROR").setText("no deleted object with id: '" + id + "' exists");
        }
    }

    public static Element list(RejectedSharedObjectRepository repo) throws SQLException {
        final Sort sort = SortUtils.createSort(RejectedSharedObject_.deletionDate);
        final List<RejectedSharedObject> all = repo.findAll(sort);


        Element deleted = new Element(SharedObjectStrategy.REPORT_ROOT);

        for (RejectedSharedObject obj : all) {
            Element delete = new Element(SharedObjectStrategy.REPORT_ELEMENT);
            String desc = obj.getDescription();
            String date = null;
            if (obj.getDeletionDate() != null) {
                date = obj.getDeletionDate().getDateAndTime();
            }

            if (date != null) {
                if (desc == null) {
                    desc = date;
                } else {
                    desc = date + " - " + desc;
                }
            }
            String id = "" + obj.getId();
            Utils.addChild(delete, SharedObjectStrategy.REPORT_ID, id);
            if (desc != null) {
                Utils.addChild(delete, SharedObjectStrategy.REPORT_DESC, desc);
                delete.setAttribute("desc", desc + " - " + date);
            }
            if (date != null) {
                Utils.addChild(delete, "date", date);
            }

            delete.addContent(new Element("xlink").setText(href(Integer.parseInt(id))));
            delete.addContent(new Element("search").setText(id + desc + date));
            delete.addContent(new Element("type").setText("deleted"));
            deleted.addContent(delete);
        }

        return deleted;
    }

    public static void delete(ServiceContext context, Integer[] ids) throws SQLException {
        final RejectedSharedObjectRepository repository = context.getBean(RejectedSharedObjectRepository.class);
        repository.deleteAll(RejectedSharedObjectSpecifications.hasId(ids));
    }

    public static String getLuceneIndexField() {
        return "xlink_deleted";
    }

    public static FindMetadataReferences createFindMetadataReferences() {
        return new FindMetadataReferences() {
            @Override
            public Query createFindMetadataQuery(String field, String concreteId, boolean validated) {
                Term term = new Term(field, WILDCARD_STRING + "id=" + concreteId + WILDCARD_STRING);
                return new WildcardQuery(term);
            }
        };
    }
}
