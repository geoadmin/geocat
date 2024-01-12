package org.fao.geonet.api.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

@RequestMapping(value = {"/{portal}/api/maintenance"})
@Tag(name = "maintenance")
@Controller("maintenance")
public class DatadirCleaner {

    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    IMetadataUtils metadataUtils;

    private BufferedWriter orphanedDataFile;
    private Path orphanedDataFilePath;

    @io.swagger.v3.oas.annotations.Operation(summary = "Clean data dir")
    @RequestMapping(
        path = "/cleanDatadir",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ObjectNode cleanDataDir() throws IOException {
        return cleanFile();
    }

    public ObjectNode cleanFile() throws IOException {
        Path rootPath = geonetworkDataDirectory.getMetadataDataDir();
        orphanedDataFilePath = rootPath.resolve("orphanedDataFiles.txt");
        try(BufferedWriter bw = Files.newBufferedWriter(orphanedDataFilePath, StandardOpenOption.WRITE)) {
            orphanedDataFile = bw;
            listFilesEatingException(rootPath) //
                .flatMap(this::listFilesEatingException)
                .filter(this::isOrphanedPath)
                .forEach(this::deleteAndLogToFile);

            ObjectNode status = new ObjectMapper().createObjectNode();
            status.put("status", "Cleaned the orphaned data: see details in " + orphanedDataFilePath.toString());
            return status;
        }
    }

    private void deleteAndLogToFile(Path path) {
        try {
            orphanedDataFile.append(path.toAbsolutePath().toString());
            orphanedDataFile.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isOrphanedPath(Path path) {
        return !metadataUtils.exists(Integer.parseInt(path.getFileName().toString()));
    }

    private Stream<Path> listFilesEatingException(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            e.printStackTrace();
            return Stream.of();
        }
    }
}
