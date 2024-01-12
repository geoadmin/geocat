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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@RequestMapping(value = {"/{portal}/api/maintenance"})
@Tag(name = "maintenance")
@Controller("maintenance")
public class DatadirCleaner {

    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    IMetadataUtils metadataUtils;

    private final AtomicInteger counter = new AtomicInteger();

    PrintWriter pwToFlush;

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
        Path orphanedDataFilePath = rootPath.resolve("orphanedDataFiles.txt");
        counter.set(0);
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(orphanedDataFilePath))) {
            pwToFlush = pw;
            listFilesEatingException(rootPath) //
                .flatMap(this::listFilesEatingException) //
                .flatMap(this::processPath) //
                .forEach(pw::println);
        }
        ObjectNode status = new ObjectMapper().createObjectNode()
            .put("status", "Cleaned the orphaned data: see details in " + orphanedDataFilePath.toString());
        return status;
    }

    private Stream<String> processPath(Path path) {
        List<String> toReturn = new ArrayList<>();
        int i = counter.incrementAndGet();
        boolean orphanedPath = false;
        try {
            orphanedPath = isOrphanedPath(path);
        } catch (RuntimeException e) {
            toReturn.add(String.format("ERROR# %s", path.toString()));
        }
        if (orphanedPath) {
            String toLog = path.toAbsolutePath().toString();
            toReturn.add(toLog);
            toReturn.add(String.format("SQL# select count(*) from metadata where id = %s", path.getFileName().toString()));
        }
        if (i % 100 == 0) {
            toReturn.add(String.format("got %d", i));
            pwToFlush.flush();
        }
        return toReturn.stream();
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
