package org.fao.geonet.api.maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@RequestMapping(value = {"/{portal}/api/maintenance"})
@Tag(name = "maintenance")
@Controller("maintenance")
public class DatadirCleaner {

    @Autowired
    GeonetworkDataDirectory geonetworkDataDirectory;

    @io.swagger.v3.oas.annotations.Operation(summary = "Clean data dir")
    @RequestMapping(
        path = "/cleanDatadir",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public Object cleanDataDir() {
        cleanFile();
        return "cleaned";
    }

    public void cleanFile() {
        Path rootPath = geonetworkDataDirectory.getMetadataDataDir();

        listFilesEatingException(rootPath) //
            .flatMap(this::listFilesEatingException)
            .map(Path::toString)
            .forEach(System.err::println);
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
