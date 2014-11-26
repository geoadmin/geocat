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
import org.fao.geonet.Util;
import org.fao.geonet.geocat.kernel.reusable.DeletedObjects;
import org.fao.geonet.repository.geocat.RejectedSharedObjectRepository;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Service for resolving an deleted reusable xlink reference
 *
 * @author jeichar
 */
public class Deleted implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        String id = Util.getParamText(params, "id");
        if (id == null || id.trim().isEmpty()) {
            return new Element("none");
        }

        // PMT c2c : fixing potential SQL injection, user input sanitization

        // Note : this has also been prevented later (pre-statement 
        // query in the DeletedObjects.get method)
        // but it is even more secure to potentially throw 
        // an exception (and stop the service execution) now as well.

        id = Integer.toString(Integer.parseInt(id));

        return DeletedObjects.get(context.getBean(RejectedSharedObjectRepository.class), id);
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

}
