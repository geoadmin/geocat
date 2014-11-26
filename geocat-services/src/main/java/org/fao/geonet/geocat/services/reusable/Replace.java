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

package org.fao.geonet.geocat.services.reusable;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.metadata.IndexRebuild;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Searches through the selected metadata and replaces all known reusable
 * objects that are identified in the metadata with the corresponding xlink
 *
 * @author jeichar
 */
public class Replace implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        SelectionManager selectionManager = SelectionManager.getManager(context.getUserSession());

        if (selectionManager == null) {
            return null;
        }

        String all = Util.getParamText(params, "all");
        boolean email = "true".equalsIgnoreCase(Util.getParamText(params, "email"));
        boolean rebuildIndex = "true".equalsIgnoreCase(Util.getParamText(params, "rebuildIndex"));
        boolean publish = "true".equalsIgnoreCase(Util.getParamText(params, "publish"));
        String ignoreErrorParam = Util.getParamText(params, "ignoreErrors");
        boolean ignoreErrors = ignoreErrorParam != null && ("true".equalsIgnoreCase(ignoreErrorParam) ||
                                                            "".equals(ignoreErrorParam.trim()));

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        Set<String> elements;
        boolean processAllRecords = "true".equalsIgnoreCase(all);
        if (processAllRecords) {
            elements = new HashSet<String>();

            String query = "SELECT id FROM Metadata";
            @SuppressWarnings("unchecked")
            List<Integer> ids = findAllIds(context);
            for (Integer id : ids) {
                elements.add("" + id);
            }

        } else {
            elements = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
        }

        int count = context.getBean(ReusableObjManager.class).process(context, elements, context.getBean(DataManager.class), email,
                processAllRecords, ignoreErrors);


        if (publish) {
            final OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
            String sql = "INSERT INTO operationallowed VALUES (?,?,0)";
            for (String id : elements) {
                int intId = Integer.parseInt(id);

                final OperationAllowedId operationAllowedId = new OperationAllowedId(intId,
                        ReservedGroup.all.getId(),
                        ReservedOperation.view.getId());
                opAllowedRepo.save(new OperationAllowed(operationAllowedId));
                operationAllowedId.setGroupId(ReservedGroup.intranet.getId());
                opAllowedRepo.save(new OperationAllowed(operationAllowedId));
            }
        }

        final DataManager dataManager = context.getBean(DataManager.class);
        if (rebuildIndex) {
            new IndexRebuild().exec(params, context);
        } else {
            for (String uuid : elements) {
                final boolean uuidIsId = processAllRecords;
                String id = ReusableObjManager.uuidToId(dataManager, uuid, uuidIsId);
                dataManager.indexMetadata(id, false);
            }

        }

        Element success = new Element("success");
        success.setText(count + " metadata elements have been analyzed and updated");
        return success;
    }

    private List<Integer> findAllIds(ServiceContext context) {
        final CriteriaBuilder cb = context.getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        final Root<Metadata> from = query.from(Metadata.class);
        query.select(from.get(Metadata_.id));
        return context.getEntityManager().createQuery(query).getResultList();
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

}
