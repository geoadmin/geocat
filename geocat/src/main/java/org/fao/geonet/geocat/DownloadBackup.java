package org.fao.geonet.geocat;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.io.File;
import java.nio.file.Path;

public class DownloadBackup implements Service {

	@Override
	public void init(Path appPath, ServiceConfig params) throws Exception {
	}

	@Override
	public Element exec(Element params, ServiceContext context)
			throws Exception {
		Log.info(ArchiveAllMetadataJob.BACKUP_LOG, "User " + context.getUserSession().getUsername() + " from IP: " + context
                .getIpAddress() + " has started to download backup archive");
		String datadir = System.getProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
		File backupDir = new File(datadir, ArchiveAllMetadataJob.BACKUP_DIR);
		if (!backupDir.exists()) {
			throw404();
		}
		File[] files = backupDir.listFiles();
		if(files == null || files.length == 0) {
			throw404();
		}
		return BinaryFile.encode(200, files[0].toPath(), false).getElement();
	}

	private void throw404() throws JeevesException {
		throw new JeevesException("Backup file does not yet exist", null) {
			private static final long serialVersionUID = 1L;
			{
				this.code = 404;
				this.id = "NoBackup";
			}
		};
	}

}
