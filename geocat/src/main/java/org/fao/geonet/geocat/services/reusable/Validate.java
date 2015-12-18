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

import com.google.common.base.Function;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.jdom.Attribute;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Makes a list of all the non-validated elements
 *
 * @author jeichar
 */
public class Validate implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        String page = Util.getParamText(params, "type");
        String[] ids = Util.getParamText(params, "id").split(",");

        Log.debug(Geocat.Module.REUSABLE, "Starting to validate following reusable objects: " + page
                                          + " \n(" + Arrays.toString(ids) + ")");

        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), context.getBean(SettingManager.class));
        SharedObjectStrategy strategy = Utils.strategy(ReusableTypes.valueOf(page), context);

        Element results = new Element("results");
        if (strategy != null) {
            results.addContent(performValidation(ids, strategy, context, baseUrl));
        }
        context.getBean(SearchManager.class).forceIndexChanges();

        Log.info(Geocat.Module.REUSABLE, "Successfully validated following reusable objects: " + page
                                         + " \n(" + Arrays.toString(ids) + ")");

        return results;
    }

    private List<Element> performValidation(String[] ids, SharedObjectStrategy strategy, ServiceContext context,
                                            String baseUrl) throws Exception {
        Map<String, String> idMapping = strategy.markAsValidated(ids, context.getUserSession());

        List<Element> result = new ArrayList<Element>();
        for (String id : ids) {
            Element e = updateXLink(strategy, context, idMapping, id, true);
            result.add(e);
        }

        Processor.clearCache();
        return result;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    private Element updateXLink(SharedObjectStrategy strategy, ServiceContext context, Map<String, String> idMapping, String id,
                                boolean validated) throws Exception {

        UserSession session = context.getUserSession();
        List<String> luceneFields = new LinkedList<String>();
        luceneFields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));

        final Function<String, String> idConverter = strategy.numericIdToConcreteId(session);
        Set<MetadataRecord> results = Utils.getReferencingMetadata(context, strategy, luceneFields, id, false, true, idConverter);

        for (MetadataRecord metadataRecord : results) {

            for (String xlinkHref : metadataRecord.xlinks) {

                @SuppressWarnings("unchecked")
                Iterator<Element> xlinks = metadataRecord.xml.getDescendants(new Utils.FindXLinks(xlinkHref));
                while (xlinks.hasNext()) {
                    Element xlink = xlinks.next();
                    xlink.removeAttribute(XLink.ROLE, XLink.NAMESPACE_XLINK);

                    String oldHref = xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);

                    //TODO: here we get an oldHref like that:
                    //   local://eng/xml.keyword.get?thesaurus=local._none_.non_validated&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23foo,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23b4ef6187-2a69-4bb2-ae3e-4bc04a25211e&multiple=true&lang=eng,ger,ita,fre,roh&textgroupOnly=true&skipdescriptivekeywords=true
                    // you can see there are two URLs separated by coma, in this example, the one we are moving
                    // and the one that needs to stay. And take into account that the one we are moving needs to
                    // be moved to another XLink that maybe has other URLs or maybe need to be created.
                    // Therefore, this whole implementation here is broken and needs to be totally rewritten.
                    // Plus, I'm not sure it's a good idea to remove the role without putting it back...
                    String newId = idMapping.get(id);
                    if (newId == null) {
                        newId = id;
                    }
                    String validateHRef = strategy.updateHrefId(oldHref, newId, session);
                    if (validated && validateHRef != null) {
                        xlink.setAttribute(XLink.HREF, validateHRef, XLink.NAMESPACE_XLINK);
                    }
                }

            }
            metadataRecord.commit(context);
        }
        Element e = new Element("id");
        e.setText(id);
        return e;
    }

}
