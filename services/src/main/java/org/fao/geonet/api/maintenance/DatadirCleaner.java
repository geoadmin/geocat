package org.fao.geonet.api.maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping(value = {"/{portal}/api/maintenance"})
@Tag(name = "maintenance")
@Controller("maintenance")
public class DatadirCleaner {

    @io.swagger.v3.oas.annotations.Operation(summary = "Clean data dir")
    @RequestMapping(
        path = "/cleanDatadir",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public Object cleanDataDir() {
        return "cleaned";
    }
}
