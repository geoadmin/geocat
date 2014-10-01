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

import com.google.common.base.Functions;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.geocat.kernel.reusable.FormatsStrategy;
import org.fao.geonet.geocat.kernel.reusable.MetadataRecord;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.kernel.reusable.Utils;
import org.fao.geonet.geocat.services.reusable.Reject;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.geocat.FormatRepository;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.Nonnull;

//=============================================================================

/**
 * Manager distribution formats (PUT/DELETE)
 *
 * @author fxprunayre
 * @see jeeves.interfaces.Service
 *
 */
public class Manager implements Service {
	/*
	 * (non-Javadoc)
	 *
	 * @see jeeves.interfaces.Service#init(String, ServiceConfig)
	 */
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jeeves.interfaces.Service#exec(org.jdom.Element, jeeves.server.context.ServiceContext)
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String action = params.getChildText(Geocat.Params.ACTION);
		String id = params.getChildText(Params.ID);
        final String name = params.getChildText(Params.NAME);
        final String version = params.getChildText(Params.VERSION);
        final boolean isValidated = Constants.toBoolean_fromYNChar(Util.getParam(params, "validated", "y").charAt(0));
        boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));

        final FormatRepository formatRepository = context.getBean(FormatRepository.class);

        Element elRes = new Element(Jeeves.Elem.RESPONSE);

        if (action.equals("DELETE")) {
            if(!Boolean.parseBoolean(Util.getParam(params, "forceDelete", "false"))) {
                Format format = formatRepository.findOne(Integer.parseInt(id));
                if (format == null) {
                    return elRes;
                }
                String msg = LangUtils.loadString("reusable.rejectDefaultMsg", context.getAppPath(), context.getLanguage());
                return new Reject().reject(context, ReusableTypes.formats, new String[]{id}, msg, null, format.isValidated(), testing);
            } else {
                formatRepository.delete(Integer.parseInt(id));
                elRes.addContent(new Element(Jeeves.Elem.OPERATION)
    					.setText(Jeeves.Text.REMOVED));
            }
        } else {
            if (id == null) {
                formatRepository.save(new Format().setName(name).setValidated(isValidated).setVersion
                        (version));
				elRes.addContent(new Element(Jeeves.Elem.OPERATION)
						.setText(Jeeves.Text.ADDED));
			} else {
                formatRepository.update(Integer.parseInt(id), new Updater<Format>() {
                    @Override
                    public void apply(@Nonnull Format entity) {
                        entity.setName(name)
                            .setVersion(version)
                            .setValidated(isValidated);
                    }
                });

				elRes.addContent(new Element(Jeeves.Elem.OPERATION)
						.setText(Jeeves.Text.UPDATED));

				Processor.uncacheXLinkUri(XLink.LOCAL_PROTOCOL+"xml.format.get?id=" + id);
                final FormatsStrategy strategy = new FormatsStrategy(formatRepository, context.getAppPath(), context.getBaseUrl(),
                        context.getLanguage());
                ArrayList<String> fields = new ArrayList<String>();

                fields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));
                fields.addAll(Arrays.asList(strategy.getValidXlinkLuceneField()));
                final Set<MetadataRecord> referencingMetadata = Utils.getReferencingMetadata(context, strategy, fields, id, null, false,
                        Functions.<String>identity());

                DataManager dm = context.getBean(DataManager.class);
                for (MetadataRecord metadataRecord : referencingMetadata) {
                    dm.indexMetadata(""+metadataRecord.id, false, true, false, false, true);
                }
            }
		}

		return elRes;
	}
}

// =============================================================================

