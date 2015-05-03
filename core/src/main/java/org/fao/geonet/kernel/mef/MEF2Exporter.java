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

package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.NoFilterFilter;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.Constants.CHARSET;
import static org.fao.geonet.constants.Geonet.IndexFieldNames.LOCALE;
import static org.fao.geonet.constants.Geonet.IndexFieldNames.UUID;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA;
import static org.fao.geonet.kernel.mef.MEFConstants.MD_DIR;
import static org.fao.geonet.kernel.mef.MEFConstants.SCHEMA;

class MEF2Exporter {
	/**
	 * Create a MEF2 file in ZIP format.
	 * 
	 * @param context
	 * @param uuids
	 *            List of records to export.
	 * @param format
	 *            {@link Format} to export.
	 * @param skipUUID
	 * @param stylePath
	 * @return MEF2 File
	 * @throws Exception
	 */
	public static Path doExport(ServiceContext context, Set<String> uuids,
            Format format, boolean skipUUID, Path stylePath, boolean resolveXlink, boolean removeXlinkAttribute,
            boolean skipError) throws Exception {

		Path file = Files.createTempFile("mef-", ".mef");
        // GEOCAT
        SearchManager searchManager = context.getBean(SearchManager.class);
        try (
                FileSystem zipFs = ZipUtil.createZipFs(file);
                IndexAndTaxonomy indexReaderAndTaxonomy = searchManager.getNewIndexReader(context.getLanguage());
        ) {
            StringBuilder builder = new StringBuilder("schema;uuid;id;title;abstract\n");
            // END GEOCAT
            for (Object uuid1 : uuids) {
                String uuid = (String) uuid1;
                try {
                    // GEOCAT
                    IndexSearcher searcher = new IndexSearcher(indexReaderAndTaxonomy.indexReader);
                    BooleanQuery query = new BooleanQuery();
                    query.add(new BooleanClause(new TermQuery(new Term(UUID, uuid)), BooleanClause.Occur.MUST));
                    query.add(new BooleanClause(new TermQuery(new Term(LOCALE, context.getLanguage())), BooleanClause.Occur.SHOULD));
                    TopDocs topDocs = searcher.search(query, NoFilterFilter.instance(), 5);
                    String mdSchema = null, mdTitle = null, mdAbstract = null, id = null;

                    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        Document doc = searcher.doc(scoreDoc.doc);
                        if (mdSchema == null) {
                            mdSchema = doc.get(Geonet.IndexFieldNames.SCHEMA);
                        }
                        if (mdTitle == null) {
                            mdTitle = doc.get("_title");
                        }
                        if (mdAbstract == null) {
                            mdAbstract = doc.get("abstract");
                        }
                        if (id == null) {
                            id = doc.get("_id");
                        }

                    }
                    builder.append(mdSchema).append(";").
                            append(uuid).append(";").
                            append(id).append(";").
                            append(mdTitle).append(";").
                            append(mdAbstract).append("\n");
                    // END GEOCAT
                    createMetadataFolder(context, uuid, zipFs, skipUUID, stylePath,
                            format, resolveXlink, removeXlinkAttribute);
                } catch (Throwable t) {
                    if (skipError) {
                        Log.error(Geonet.MEF, "Error exporting metadata to MEF file: " + uuid1, t);
                    } else {
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        }
                        throw new RuntimeException(t);
                    }
                }
            }
            // GEOCAT
            Files.write(zipFs.getPath("/index.csv"), builder.toString().getBytes(Constants.CHARSET));
            // END GEOCAT
        }
		return file;
	}

	/**
	 * Create a metadata folder according to MEF {@link Version} 2
	 * specification. If current record is based on an ISO profil, the
	 * stylesheet /convert/to19139.xsl is used to map to ISO. Both files are
	 * included in MEF file. Export relevant information according to format
	 * parameter.
	 * 
	 * @param context
	 * @param uuid
	 *            Metadata record to export
	 * @param zipFs
	 *            Zip file to add new record
	 * @param skipUUID
	 * @param stylePath
	 * @param format
	 * @throws Exception
	 */
	private static void createMetadataFolder(ServiceContext context,
			String uuid, FileSystem zipFs, boolean skipUUID,
			Path stylePath, Format format, boolean resolveXlink, boolean removeXlinkAttribute) throws Exception {

        final Path metadataRootDir = zipFs.getPath(uuid);
        Files.createDirectories(metadataRootDir);

		Pair<Metadata, String> recordAndMetadataForExport =
				MEFLib.retrieveMetadata(context, uuid, resolveXlink, removeXlinkAttribute);
		Metadata record = recordAndMetadataForExport.one();
		String xmlDocumentAsString = recordAndMetadataForExport.two();

		String id = "" + record.getId();
		String isTemp = record.getDataInfo().getType().codeString;

		if (!"y".equals(isTemp) && !"n".equals(isTemp))
			throw new Exception("Cannot export sub template");

		Path pubDir = Lib.resource.getDir(context, "public", id);
        Path priDir = Lib.resource.getDir(context, "private", id);

        final Path metadataXmlDir = metadataRootDir.resolve(MD_DIR);
        Files.createDirectories(metadataXmlDir);

        Collection<ExportFormat> formats = context.getApplicationContext().getBeansOfType(ExportFormat.class).values();
        for (ExportFormat exportFormat : formats) {
            for (Pair<String, String> output : exportFormat.getFormats(context, record)) {
                Files.write(metadataXmlDir.resolve(output.one()), output.two().getBytes(CHARSET));
            }
        }

		// --- save native metadata
        Files.write(metadataXmlDir.resolve(FILE_METADATA), xmlDocumentAsString.getBytes(CHARSET));


		// --- save Feature Catalog
		String ftUUID = getFeatureCatalogID(context, record.getId());
		if (!ftUUID.equals("")) {
			Pair<Metadata, String> ftrecordAndMetadata = MEFLib.retrieveMetadata(context, ftUUID, resolveXlink, removeXlinkAttribute);
            Path featureMdDir = metadataRootDir.resolve(SCHEMA);
            Files.createDirectories(featureMdDir);
            Files.write(featureMdDir.resolve(FILE_METADATA), ftrecordAndMetadata.two().getBytes(CHARSET));
        }


		// --- save info file
		byte[] binData = MEFLib.buildInfoFile(context, record, format, pubDir,
				priDir, skipUUID).getBytes(Constants.ENCODING);

        Files.write(metadataRootDir.resolve(FILE_INFO), binData);

		// --- save thumbnails and maps

		if (format == Format.PARTIAL || format == Format.FULL) {
            IO.copyDirectoryOrFile(pubDir, metadataRootDir, true);
        }

		if (format == Format.FULL) {
			try {
                Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
                IO.copyDirectoryOrFile(priDir, metadataRootDir, true);
			} catch (Exception e) {
				// Current user could not download private data
			}
		}
	}

	/**
	 * Get Feature Catalog ID if exists using relation table.
	 * 
	 * @param context
	 * @param metadataId
	 *            Metadata record id to search for feature catalogue for.
	 * @return String Feature catalogue uuid.
	 * @throws Exception
	 */
	private static String getFeatureCatalogID(ServiceContext context, int metadataId) throws Exception {
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);

        List<MetadataRelation> relations = context.getBean(MetadataRelationRepository.class).findAllById_MetadataId(metadataId);

		if (relations.isEmpty()) {
			return "";
        }

		// Assume only one feature catalogue is available for a metadata record.
		int ftId =  relations.get(0).getId().getRelatedId();

        String ftUuid = dm.getMetadataUuid("" + ftId);

		return ftUuid != null ? ftUuid : "";
	}
}
