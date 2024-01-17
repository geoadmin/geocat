package org.fao.geonet.api.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;

@RequestMapping(value = {"/{portal}/api/maintenance"})
@Tag(name = "maintenance")
@Controller("maintenance")
public class DatadirCleaner {

    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    IMetadataUtils metadataUtils;

    private final AtomicInteger processedPathCounter = new AtomicInteger();
    private final AtomicInteger notDeletedPathCounter = new AtomicInteger();

    PrintWriter pwToFlush;

    @io.swagger.v3.oas.annotations.Operation(summary = "Clean data dir", description = "Search for dangling metadata " +
        "in data dir, and delete them since they are no longer referenced in the database. Please use cautiously.")
    @RequestMapping(
        path = "/cleanDatadir",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public synchronized ObjectNode cleanDataDir(@PathVariable(value = "portal") final String portal) throws IOException {
        processedPathCounter.set(0);
        notDeletedPathCounter.set(0);
        final Path orphanedDataFilePath = cleanFile();
        return new ObjectMapper().createObjectNode() //
            .put("status", format("Cleaned the orphaned data: see details in %s.", orphanedDataFilePath)) //
            .put("pathsCounters", format("Kept %d paths out of %d.", notDeletedPathCounter.get(), processedPathCounter.get()))
            .put("warning", format("Although the portal %s was defined, it clears orphaned data without knowledge of the portal,", portal));
    }

    public Path cleanFile() throws IOException {
        final Path rootPath = geonetworkDataDirectory.getMetadataDataDir();
        final Path orphanedDataReportFilePath = rootPath.resolve("orphanedDataFiles.txt");
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(orphanedDataReportFilePath))) {
            pwToFlush = pw;
            listFiles(rootPath) //
                .flatMap(this::listFiles) //
                .flatMap(this::processPath) //
                .forEach(pw::println);
        }
        return orphanedDataReportFilePath;
    }

    private Stream<String> processPath(final Path path) {
        final Stream.Builder<String> toLog = Stream.builder();
        if (isOrphanedPath(path, toLog)) {
            FileUtils.deleteQuietly(path.toFile().getAbsoluteFile());
            toLog.add(path.toAbsolutePath().toString());
            toLog.add(format("SQL# select count(*) from metadata where id = %s;", path.getFileName()));
        } else {
            notDeletedPathCounter.incrementAndGet();
        }
        if (processedPathCounter.incrementAndGet() % 100 == 0) {
            toLog.add(format("Processed %d paths.", processedPathCounter.get()));
            pwToFlush.flush();
        }
        return toLog.build();
    }

    private boolean isOrphanedPath(final Path path, final Stream.Builder<String> toLog) {
        try {
            return !metadataUtils.exists(Integer.parseInt(path.getFileName().toString()));
        } catch (RuntimeException e) {
            toLog.add(format("ERROR# %s.", path));
            return false;
        }
    }

    private Stream<Path> listFiles(final Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new RuntimeException(format("Failed to access path %s.", path), e);
        }
    }
}
