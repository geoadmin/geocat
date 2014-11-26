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

package org.fao.geonet.geocat.services.extent;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.extent.ExtentSelection;
import org.fao.geonet.geocat.kernel.extent.FeatureType;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Set;

import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.getSelection;

/**
 * Allows for selecting one or more extents for operations such as deletion
 *
 * @author jeichar
 */
public class Select implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        final ExtentManager extentMan = context.getBean(ExtentManager.class);

        final ExtentSelection selection = getSelection(context);

        final Element success = new Element("success");

        final Set<Pair<FeatureType, String>> selectionIds = selection.getIds();
        synchronized (selectionIds) {

            @SuppressWarnings("unchecked")
            java.util.Iterator<Object> iter = params.getChildren().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof Element) {
                    Element elem = (Element) next;
                    if (elem.getName().equalsIgnoreCase("add")) {
                        String typename = elem.getAttributeValue("typename");
                        String id = elem.getAttributeValue("id");
                        FeatureType ft = extentMan.getSource().getFeatureType(typename);
                        selectionIds.add(Pair.read(ft, id));
                    } else if (elem.getName().equalsIgnoreCase("remove")) {
                        String typename = elem.getAttributeValue("typename");
                        String id = elem.getAttributeValue("id");
                        FeatureType ft = extentMan.getSource().getFeatureType(typename);
                        selectionIds.remove(Pair.read(ft, id));
                    }
                }
            }
            success.setAttribute("count", String.valueOf(selectionIds.size()));
        }

        return success;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

}
