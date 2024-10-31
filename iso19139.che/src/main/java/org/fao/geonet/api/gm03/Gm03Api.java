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

package org.fao.geonet.api.gm03;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;

import static org.fao.geonet.api.ApiParams.*;


@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordsGm03")
@ReadWriteController
public class Gm03Api  {

    private final String GM03_XSL_CONVERSION_FILE= "convert/ISO19139CHE-to-GM03.xsl";
    private final String GM03_XSD_FILE = "GM03_2_1.xsd";

    @Autowired
    SchemaManager _schemaManager;

    @Autowired
    DataManager _dataManager;

    @Operation(summary = "Get a metadata record as full GM03")
    @RequestMapping(value = "/{portal}/api/records/{metadataUuid}/formatters/gm03",
            method = RequestMethod.GET,
            produces = {
                    MediaType.APPLICATION_XML_VALUE
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return the record in GM03 format."),
            @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public
    @ResponseBody
    Element getRecordAsGM03(
            @Parameter(
                    description = API_PARAM_RECORD_UUID,
                    required = true)
            @PathVariable
                    String metadataUuid,
            @RequestHeader(
                    value = HttpHeaders.ACCEPT,
                    defaultValue = MediaType.APPLICATION_XML_VALUE
            )
            String acceptHeader,
            HttpServletResponse response,
            HttpServletRequest request
    )
            throws Exception {

        AbstractMetadata metadata;

        try {
            metadata = ApiUtils.canViewRecord(metadataUuid, request);
        } catch (ResourceNotFoundException e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw e;
        }
        catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }
        ServiceContext context = ApiUtils.createServiceContext(request);
        try {
            Lib.resource.checkPrivilege(context,
                    String.valueOf(metadata.getId()),
                    ReservedOperation.view);
        } catch (Exception e) {
            Log.debug(API.LOG_MODULE_NAME, e.getMessage(), e);
            throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
        }

        Element xml  = _dataManager.getMetadataNoInfo(context, metadata.getId() + "");

        final String schema = metadata.getDataInfo().getSchemaId();
        Path schemaDir = null;
        if (schema != null) {
            schemaDir = _schemaManager.getSchemaDir(schema);
        }

        DOMOutputter outputter = new DOMOutputter();
        Document domIn = outputter.output(new org.jdom.Document(xml));

        Path xsdFile = schemaDir.resolve(GM03_XSD_FILE);
        Path xsl = schemaDir.resolve(GM03_XSL_CONVERSION_FILE);

        ISO19139CHEtoGM03Base toGm03 = new ISO19139CHEtoGM03(xsdFile, xsl);
        Document domOut = toGm03.convert(domIn);

        DOMBuilder builder = new DOMBuilder();
        Element rootElement = builder.build(domOut).getRootElement();
        rootElement.detach();
        return rootElement;
    }


}
