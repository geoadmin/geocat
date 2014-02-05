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

package org.fao.geonet.services.metadata.schema;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.Updater;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * This class returns if a schema is mandatory or not
 * 
 * @author delawen
 */
public class IsRequired implements Service {
    public static final String PARAM_SCHEMATRON = "schematron";

	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String action = null;
		try {
			action = Util.getParam(params, "action");
		} catch (MissingParameterEx ex) {
		}

        SchematronRepository repo = context.getBean(SchematronRepository.class);

		String schema = Util.getParam(params, PARAM_SCHEMATRON);
		final Integer schematronId = Integer.valueOf(schema);

		if ("toggle".equalsIgnoreCase(action)) {
            repo.update(schematronId, new Updater<Schematron>() {
                @Override
                public void apply(@Nonnull Schematron entity) {
                    Boolean updatedRequired = entity.getRequired();

                    entity.setRequired(updatedRequired == null || !updatedRequired);
                }
            });
		}

        final Schematron schematron = repo.findOne(schematronId);

        Element result = new Element("record").addContent(
                new Element("required").setText(schematron.getRequiredAsString())
        );

		return result;

	}

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

}
