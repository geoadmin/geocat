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
import org.fao.geonet.domain.SchematronCriteria;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.jdom.Element;

/**
 * This class manages the configuration of the validation framework with
 * parameters "add" and "delete"
 * 
 * @author delawen
 */
public class Validation implements Service {

	public Element exec(Element params, ServiceContext context)
			throws Exception {

		Element res = new Element("schematron");

		String action = null;
		try {
			action = Util.getParam(params, "action");
			context.info("Action: " + action);
		} catch (MissingParameterEx ex) {
		}

		final SchematronCriteriaRepository schcrrep = context
				.getBean(SchematronCriteriaRepository.class);
		final SchematronRepository schrep = context
				.getBean(SchematronRepository.class);

		// Do action
		if ("delete".equalsIgnoreCase(action)) {
			SchematronCriteria sc = schcrrep.findOne(Integer.valueOf(Util
					.getParam(params, "id")));
			if (sc != null) {
				schcrrep.delete(sc);
				schcrrep.flush();
			}
		} else if ("add".equalsIgnoreCase(action)) {
			String schema = Util.getParam(params, "schematron");
			Schematron schematron = schrep.findOne(Integer.valueOf(schema));

			SchematronCriteria sc = new SchematronCriteria();
			sc.setSchematron(schematron);
			sc.setType(SchematronCriteriaType.valueOf(Util.getParam(params,
					"type")));
			sc.setValue(Util.getParam(params, "value"));
			schcrrep.saveAndFlush(sc);
		}

		// Return the current lists
		for (SchematronCriteria s : schcrrep.findAll()) {
			res.addContent(s.asXml().setName("criteria"));
		}

		for (Schematron s : schrep.findAll()) {
			res.addContent(s.asXml().setName("schematron"));
		}

		return res;
	}
	public void init(String appPath, ServiceConfig params) throws Exception {
	}

}
