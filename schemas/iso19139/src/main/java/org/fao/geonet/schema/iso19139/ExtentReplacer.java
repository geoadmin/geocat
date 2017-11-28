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

package org.fao.geonet.schema.iso19139;

import org.fao.geonet.kernel.schema.subtemplate.AbstractReplacer;
import org.fao.geonet.kernel.schema.subtemplate.ConstantsProxy;
import org.fao.geonet.kernel.schema.subtemplate.ManagersProxy;
import org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;
import java.util.stream.Collectors;

public class ExtentReplacer extends AbstractReplacer {

    public ExtentReplacer(List<Namespace> namespaces,
                          ManagersProxy managersProxy,
                          ConstantsProxy constantsProxy) {
        super(namespaces, managersProxy, constantsProxy);
    }

    @Override
    public String getAlias() {
        return SubtemplatesByLocalXLinksReplacer.EXTENT;
    }

    @Override
    protected String getElemXPath() {
        return ".//gmd:extent";
    }

    @Override
    protected void queryAddExtraClauses(QueryWithCounter query, Element extent, String lang) throws Exception {
        String title = getFieldValue(extent, ".//gmd:description", lang);
        if (title.length() != 0) {
            addWeightingClause(query, "__title", title);
        } else {
            title = ((List<Element>) Xml.selectNodes(extent, ".//gco:Decimal", namespaces)).stream()
                    .map(elem -> elem.getText())
                    .collect(Collectors.joining(", "));
            addWeightingClause(query, "__title", title);
        }
    }
}
