//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.webdav;

import com.google.common.base.Optional;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.HarvesterDataRepository;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.HarvestValidationEnum;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.harvesting.Util;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

//=============================================================================

class Harvester extends BaseAligner implements IHarvester<HarvestResult> {
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	public Harvester(Logger log, ServiceContext context, WebDavParams params) {
		this.log    = log;
		this.context= context;
		this.params = params;

		result = new HarvestResult();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
		schemaMan = gc.getBean(SchemaManager.class);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
	@Override
	public HarvestResult harvest(Logger log) throws Exception {
		this.log = log;
        if(log.isDebugEnabled()) log.debug("Retrieving remote metadata information for : "+ params.name);
        RemoteRetriever rr = null;
        if (params.subtype.equals("webdav")) {
            rr = new WebDavRetriever();
        } else if (params.subtype.equals("waf")) {
            rr = new WAFRetriever();
        } else {
            throw new IllegalArgumentException(params.subtype + " is not one of webdav or waf");
        }
        try {
            Log.info(Log.SERVICE, "webdav harvest subtype : "+params.subtype);
            rr.init(log, context, params);
            List<RemoteFile> files = rr.retrieve();
            if(log.isDebugEnabled()) log.debug("Remote files found : "+ files.size());
            align(files);
        } finally { rr.destroy();
        }
		return result;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void align(final List<RemoteFile> files) throws Exception {
		log.info("Start of alignment for : "+ params.name);
		//-----------------------------------------------------------------------
		//--- retrieve all local categories and groups
		//--- retrieve harvested uuids for given harvesting node
		localCateg = new CategoryMapper(context);
		localGroups= new GroupMapper(context);
		localUris  = new UriMapper(context, params.uuid);

		//-----------------------------------------------------------------------
		//--- remove old metadata
		for (final String uri : localUris.getUris()) {
            if (!exists(files, uri)) {
                // only one metadata record created per uri by this harvester
                String id = localUris.getRecords(uri).get(0).id;
                if (log.isDebugEnabled()){
                    log.debug("  - Removing old metadata with local id:"+ id);
                }
                try {
                    dataMan.deleteMetadataGroup(context, id);
                } catch (Exception e) {
                    log.error("Error occurred while deleting metadata id");
                }
                dataMan.flush();
                result.locallyRemoved++;

            }
		}
		//-----------------------------------------------------------------------
		//--- insert/update new metadata

		for(RemoteFile rf : files) {
			result.totalMetadata++;
			List<RecordInfo> records = localUris.getRecords(rf.getPath());
			if (records == null) {
				addMetadata(rf);
			} else {
				// only one metadata record created per uri by this harvester 
				updateMetadata(rf, records.get(0));
			}
		}
		log.info("End of alignment for : "+ params.name);
	}

	//--------------------------------------------------------------------------
	/** Returns true if the uri is present in the remote folder */
	private boolean exists(List<RemoteFile> files, String uri) {
		for(RemoteFile rf : files) {
			if (uri.equals(rf.getPath())) {
				return true;
			}
		}
		return false;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : addMetadata
	//---
	//--------------------------------------------------------------------------
	/**
	 * 
	 To determine the UUID we are going to use the following mechanism: 1.-
	 * Look for the file identifier on the metadata xml 2.- If there is no file
	 * identifier, then use the name of the file 3.- If there is a collision of
	 * uuid with existent metadata, use a random one 4.- If we still don't have
	 * a clear UUID, use a random one (backup plan)
	 * 
	 **/
	private void addMetadata(RemoteFile rf) throws Exception {
		Element md = retrieveMetadata(rf);
		if (md == null) {
			return;
		}
		//--- schema handled check already done
		String schema = dataMan.autodetectSchema(md);

        // 1.- Look for the file identifier on the metadata xml
        String uuid = dataMan.extractUUID(schema,  md);
        // GEOCAT
        if (uuid == null && schema.equals("iso19139.che")) {
            return;
        }
        // END GEOCAT

        // 2.- If there is no file identifier, then use the name of the file
        if (uuid == null) {
                String path = rf.getPath();
                int start = path.lastIndexOf("/") + 1;
                uuid = path.substring(start, path.length() - 4);
        }

        // 3.- If there is a collision of uuid with existent metadata, use a
        // random one
        if (dataMan.existsMetadataUuid(uuid)) {
                uuid = null;
        }

        // 4.- If we still don't have a clear UUID, use a random one (backup
        // plan)
        if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                log.debug("  - Setting uuid for metadata with remote path : "
                                + rf.getPath());

                // --- set uuid inside metadata and get new xml
                try {
                        md = dataMan.setUUID(schema, uuid, md);
                } catch (Exception e) {
                        log.error("  - Failed to set uuid for metadata with remote path : "
                                        + rf.getPath());
                        errors.add(new HarvestError(e, this.log));
                        return;
                }
        }

        log.debug("  - Adding metadata with remote path : " + rf.getPath());



        if(log.isDebugEnabled()) log.debug("  - Adding metadata with remote path : "+ rf.getPath());

		//
        // insert metadata
        //
        String group = null, isTemplate = null, docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        String id = dataMan.insertMetadata(context, schema, md, uuid, Integer.parseInt(params.ownerId), group, params.uuid,
                     isTemplate, docType, category, rf.getChangeDate().getDateAndTime(), rf.getChangeDate().getDateAndTime(), ufo, indexImmediate);


		int iId = Integer.parseInt(id);

		dataMan.setTemplateExt(iId, MetadataType.METADATA);
		dataMan.setHarvestedExt(iId, params.uuid, Optional.of(rf.getPath()));

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);
        addCategories(id, params.getCategories(), localCateg, dataMan, context, log, null);

        dataMan.flush();

        dataMan.indexMetadata(id, false, context);
		result.addedMetadata++;
	}
	
	//--------------------------------------------------------------------------

	private Element retrieveMetadata(RemoteFile rf) {
		try {
            if(log.isDebugEnabled()) log.debug("Getting remote file : "+ rf.getPath());
			Element md = rf.getMetadata(schemaMan);
            if(log.isDebugEnabled()) log.debug("Record got:\n"+ Xml.getString(md));

            // GEOCAT
            String schema;
            try {
                schema = dataMan.autodetectSchema(md);
            } catch (NoSchemaMatchesException e) {
                if (md.getName() == "TRANSFER") {
                    // we need to convert to CHE
                    String styleSheetPath = context.getAppPath() + "xsl/conversion/import/GM03-to-ISO19139CHE.xsl";
                    Element tmp = Xml.transform(md, styleSheetPath);
                    md = tmp;
                }
                schema = dataMan.autodetectSchema(md);
            }
            if (params.validate == HarvestValidationEnum.NOVALIDATION || validates(schema, md)) {
                return (Element) md.detach();
            } else {

                if (params.validate == HarvestValidationEnum.NOVALIDATION || validates(schema, md)) {
                    return (Element) md.detach();
                }
                log.warning("Skipping metadata that does not validate. Path is : "+ rf.getPath());
                result.doesNotValidate++;
            }
            // END GEOCAT

			log.warning("Skipping metadata that does not validate. Path is : "+ rf.getPath());
			result.doesNotValidate++;
		}
		catch (NoSchemaMatchesException e) {
				log.warning("Skipping metadata with unknown schema. Path is : "+ rf.getPath());
				result.unknownSchema++;
		}
		catch(JDOMException e) {
			log.warning("Skipping metadata with bad XML format. Path is : "+ rf.getPath());
			// GEOCAT: issue #21546 : warn the admin by mail
			Util.warnAdminByMail(context, Util.SKEL_MAIL_WEBDAV_ERROR, params.url, null, rf.getPath());
            // END GEOCAT
			result.badFormat++;
		}
		catch(Exception e) {
			log.warning("Raised exception while getting metadata file : "+ e);

			// GEOCAT: issue #21546 : warn the admin by mail
			Util.warnAdminByMail(context, Util.SKEL_MAIL_WEBDAV_ERROR, params.url, null, rf.getPath());
            // END GEOCAT
			result.unretrievable++;
		}
		//--- we don't raise any exception here. Just try to go on
		return null;
	}

	//--------------------------------------------------------------------------

	private boolean validates(String schema, Element md) {
		try {
			dataMan.validate(schema, md);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods : updateMetadata
	//---
	//--------------------------------------------------------------------------

	private void updateMetadata(RemoteFile rf, RecordInfo record) throws Exception {
		if (!rf.isMoreRecentThan(record.changeDate)) {
            if(log.isDebugEnabled()) log.debug("  - Metadata XML not changed for path : "+ rf.getPath());
			result.unchangedMetadata++;
		}
		else {
            if(log.isDebugEnabled()) log.debug("  - Updating local metadata for path : "+ rf.getPath());
			Element md = retrieveMetadata(rf);
			if (md == null) {
				return;
			}
			
			//--- set uuid inside metadata (on metadata add it's created a new uuid ignoring fileIdentifier uuid).
            //--- In update we should use db uuid to update the xml uuid and keep in sych both.
            try {
                String schema = dataMan.autodetectSchema(md);
                
                //Update only if different
                String uuid = dataMan.extractUUID(schema,  md);
                if (!record.uuid.equals(uuid)) {
                	md = dataMan.setUUID(schema, record.uuid, md);
                }
            } catch(Exception e) {
                log.error("  - Failed to set uuid for metadata with remote path : "+ rf.getPath());
                return;
            }

            //
            // update metadata
            //
            boolean validate = false;
            boolean ufo = false;
            boolean index = false;
            String language = context.getLanguage();
            final Metadata metadata = dataMan.updateMetadata(context, record.id, md, validate, ufo, index, language,
                    rf.getChangeDate().getDateAndTime(), false, false);

            //--- the administrator could change privileges and categories using the
			//--- web interface so we have to re-set both
            OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
            repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(record.id));
            addPrivileges(record.id, params.getPrivileges(), localGroups, dataMan, context, log);

            metadata.getCategories().clear();
            context.getBean(MetadataRepository.class).save(metadata);
            addCategories(record.id, params.getCategories(), localCateg, dataMan, context, log, null);

            dataMan.flush();

            dataMan.indexMetadata(record.id, false, context);
			result.updatedMetadata++;
		}
	}

	public List<HarvestError> getErrors() {
		return errors;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger log;
	private ServiceContext context;
	private WebDavParams params;
	private DataManager dataMan;
	private CategoryMapper localCateg;
	private GroupMapper localGroups;
	private UriMapper localUris;
	private HarvestResult result;
	private SchemaManager  schemaMan;
    private List<HarvestError> errors = new LinkedList<HarvestError>();
}

//=============================================================================

interface RemoteRetriever {
	public void init(Logger log, ServiceContext context, WebDavParams params);
	public List<RemoteFile> retrieve() throws Exception;
	public void destroy();
}

//=============================================================================

interface RemoteFile {
	public String  getPath();
	public ISODate getChangeDate();
	public Element getMetadata(SchemaManager  schemaMan) throws Exception;
	public boolean isMoreRecentThan(String localDate);
}

//=============================================================================
