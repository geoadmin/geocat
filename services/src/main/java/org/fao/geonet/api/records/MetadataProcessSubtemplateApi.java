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

package org.fao.geonet.api.records;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.subtemplate.SubtemplateAwareSchemaPlugin;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

import static org.fao.geonet.api.ApiParams.*;

@RequestMapping(value = { "/{portal}/api/records", "/{portal}/api/" + API.VERSION_0_1 + "/records" })
@Api(value = API_CLASS_RECORD_TAG, tags = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordSubtemplatesProcessing")
@ReadWriteController
public class MetadataProcessSubtemplateApi {

    @Autowired
    private IMetadataManager metadataManager;

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private SettingManager settingManager;


    @ApiOperation(value = "Substitute md subtemplates by xlinks", notes = API_OP_NOTE_PROCESS, nickname = "processRecordSubtemplates")
    @RequestMapping(value = "/{metadataUuid}/processRecordSubtemplates", method = {
            RequestMethod.POST, }, produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Record processed and saved."),
            @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT) })
    public @ResponseBody ResponseEntity processRecord(
            @ApiParam(value = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
            HttpServletRequest request)
            throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);
        process(context, metadata);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void process(ServiceContext context, AbstractMetadata metadata) throws Exception {

        Element beforeMetadata = metadataManager.getMetadata(context, Integer.toString(metadata.getId()), false, true, false, false);
        String schema = metadataSchemaUtils.autodetectSchema(beforeMetadata);
        MetadataSchema metadataSchema = schemaManager.getSchema(schema);

        if (!(metadataSchema.getSchemaPlugin() instanceof SubtemplateAwareSchemaPlugin)) {
            return;
        }

        String templatesToOperateOn = settingManager.getValue(Settings.SYSTEM_XLINK_TEMPLATES_TO_OPERATE_ON_AT_INSERT);

        Element processedMetadata = ((SubtemplateAwareSchemaPlugin) metadataSchema.getSchemaPlugin())
            .replaceSubtemplatesByLocalXLinks(
                beforeMetadata,
                templatesToOperateOn);

        metadataManager.updateMetadata(context, "" + metadata.getId(), processedMetadata, false, true, true, context.getLanguage(), new ISODate().toString(), true);
    }
}
