//=============================================================================
//===	Copyright (C) 2008 Swisstopo, BRGM
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - email: GeoNetwork@osgeo.org
//==============================================================================

package org.fao.geonet.geocat.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.XLink;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/**
 * For editing : adds a tag to a metadata. Access is restricted
 * <p/>
 * <ul>
 * <li>ID : Identifier of the metadata record to update</li>
 * <li>REF : Reference of the metadata element to be updated</li>
 * <li>NAME : Name of the metadata element to be updated</li>
 * <li>VERSION : Current metadata version in edition</li>
 * <li>HREF : href of the XLink to be created</li>
 * </ul>
 *
 * @author fxprunayre
 */
public class AddXLink implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
            throws Exception {

        UserSession session = context.getUserSession();

        String id = Util.getParam(params, Params.ID);
        String ref = Util.getParam(params, Params.REF);
        String name = Util.getParam(params, Params.NAME);
        String href = Util.getParam(params, XLink.HREF);


        final ReusableObjManager reusableObjManager = context.getBean(ReusableObjManager.class);
        String role = reusableObjManager.isValidated(href, context) ? "" : ReusableObjManager.NON_VALID_ROLE;
        href = reusableObjManager.createAsNeeded(href, context);
        context.debug("Add as xlink url: " + href);

        XLink xLink = new XLink(href, "", role);

        // -- build the element to be added and return it
        AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        Element element = ajaxEditUtils.addXLink(session, id, ref, name, xLink);
        EditLib.tagForDisplay(element);
        Element md = (Element) findRoot(element).clone();
        EditLib.removeDisplayTag(element);
        return md;
    }

    private Element findRoot(Element element) {
        if (element.isRootElement() || element.getParentElement() == null) return element;
        return findRoot(element.getParentElement());
    }
}

// =============================================================================

