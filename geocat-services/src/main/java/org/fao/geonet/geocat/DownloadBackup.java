package org.fao.geonet.geocat;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import javax.servlet.http.HttpServletRequest;

@Controller
public class DownloadBackup {
    @Autowired
    ServiceManager serviceManager;
    @RequestMapping(value="/{lang}/download.backup",produces = "application/zip")
    @ResponseBody
    public ResponseEntity<FileSystemResource> exec(@PathVariable String lang, HttpServletRequest request) throws Exception {
        ServiceContext context = serviceManager.createServiceContext("download.backup", lang, request);
        Log.info(ArchiveAllMetadataJob.BACKUP_LOG, "User " + context.getUserSession().getUsername() + " from IP: " + context
                .getIpAddress() + " has started to download backup archive");
        String datadir = System.getProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
        File backupDir = new File(datadir, ArchiveAllMetadataJob.BACKUP_DIR);
        if (!backupDir.exists()) {
            throw404();
        }
        File[] files = backupDir.listFiles();
        if (files == null || files.length == 0) {
            throw404();
        }
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("content-disposition", "attachment; filename=" + files[0].getName());

        final ResponseEntity<FileSystemResource> response = new ResponseEntity<>(new FileSystemResource(files[0]), headers, HttpStatus.OK);

        return response;
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
