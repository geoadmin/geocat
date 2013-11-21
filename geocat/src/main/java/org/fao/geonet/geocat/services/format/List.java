//=============================================================================
//===	Copyright (C) 2008 Swisstopo
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

package org.fao.geonet.geocat.services.format;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.domain.geocat.Format_;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.geocat.FormatRepository;
import org.fao.geonet.repository.geocat.specification.FormatSpecs;
import org.jdom.Content;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//=============================================================================

/**
 * Retrieves all format in the system if no name parameter
 * provided.
 *
 * @author fxprunayre
 * @see jeeves.interfaces.Service
 */

public class List implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }


    public Element exec(Element params, ServiceContext context) throws Exception {

        boolean orderByValidated = "validated".equalsIgnoreCase(params.getChildTextNormalize("order"));

        final FormatRepository formatRepository = context.getBean(FormatRepository.class);
        String name = params.getChildText(Params.NAME);
        Element el = null;

        final Sort sort;

        if (orderByValidated) {
            sort = new Sort(new Sort.Order(Sort.Direction.ASC, SortUtils.createPath(Format_.jpaWorkaround_validated)),
                    new Sort.Order(SortUtils.createPath(Format_.name)),
                    new Sort.Order(SortUtils.createPath(Format_.version)));
        } else {
            sort = SortUtils.createSort(Format_.name, Format_.version);
        }


        Element all;
        if (name == null)
            all = formatRepository.findAllAsXml(sort);
        else {
            all = formatRepository.findAllAsXml(FormatSpecs.nameContains(name), sort);
        }

        return el;
    }
}

//=============================================================================

