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
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.geocat.kernel.reusable.DeletedObjects;
import org.fao.geonet.geocat.kernel.reusable.FindMetadataReferences;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.GeocatXslUtil;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.util.LangUtils.iso19139DefaultLang;

/**
 * Return the metadata that references the reusable object
 *
 * @author jeichar
 */
public class ReferencingMetadata implements Service
{

    @SuppressWarnings("unchecked")
	public Element exec(Element params, ServiceContext context) throws Exception
    {

        String id = Util.getParam(params, "id");
        String type = Util.getParam(params, "type");
        boolean validated = Util.getParam(params, "validated", false);

        List<String> fields = new LinkedList<String>();
        Function<String,String> idConverter;
        FindMetadataReferences findResources;

        if (type.equalsIgnoreCase("deleted")) {
            findResources = DeletedObjects.createFindMetadataReferences();
            fields.addAll(Arrays.asList(DeletedObjects.getLuceneIndexField()));
            idConverter= SharedObjectStrategy.ID_FUNC;
        } else {
            final SharedObjectStrategy sharedObjectStrategy = Utils.strategy(ReusableTypes.valueOf(type), context);
            if (validated) {
                fields.addAll(Arrays.asList(sharedObjectStrategy.getValidXlinkLuceneField()));
            } else {
                fields.addAll(Arrays.asList(sharedObjectStrategy.getInvalidXlinkLuceneField()));
            }
            idConverter= sharedObjectStrategy.numericIdToConcreteId(context.getUserSession());
            findResources = sharedObjectStrategy;
        }

        Set<MetadataRecord> md = Utils.getReferencingMetadata(context, findResources, fields, id, validated, true, idConverter);
        UserRepository userRepository = context.getBean(UserRepository.class);
        Element response = new Element("response");
        for (MetadataRecord metadataRecord : md) {
            
            Element record = new Element("record");
            response.addContent(record);

            Utils.addChild(record, "id", ""+metadataRecord.id);

            try {
                Element titleElement = metadataRecord.xml.getChild("identificationInfo", Geonet.Namespaces.GMD).getChild(
                        "CHE_MD_DataIdentification", GeocatXslUtil.CHE_NAMESPACE).getChild("citation", Geonet.Namespaces.GMD).getChild(
                        "CI_Citation", Geonet.Namespaces.GMD).getChild("title", Geonet.Namespaces.GMD);

                String translated = LangUtils.iso19139TranslatedText(titleElement, context.getLanguage(),
                        iso19139DefaultLang(metadataRecord.xml));

                Utils.addChild(record, "title", translated);
            } catch (NullPointerException e) {
                // the get title code is brittle so catch null pointer and
                // assume there is no title:

                Utils.addChild(record, "title", "n/a");
            }
            Utils.addChild(record, "name", metadataRecord.name(userRepository));
            Utils.addChild(record, "email", metadataRecord.email(userRepository));
        }

        return response;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception
    {
    }

}
