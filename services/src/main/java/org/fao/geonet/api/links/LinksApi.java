//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.api.links;

import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.management.MalformedObjectNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.url.UrlAnalyzer;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.LinkSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.google.common.collect.Sets;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import springfox.documentation.annotations.ApiIgnore;
@EnableWebMvc
@Service
@RestController
@RequestMapping(value = {
    "/{portal}/api/records/links",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records/links"
})
@Api(value = "links",
    tags = "links",
    description = "Record link operations")
public class LinksApi {
    private static final int NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP = 5;
    @Autowired
    protected ApplicationContext appContext;
    @Autowired
    LinkRepository linkRepository;
    @Autowired
    IMetadataUtils metadataUtils;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    UrlAnalyzer urlAnalyser;
    @Autowired
    MBeanExporter mBeanExporter;
    @Autowired
    AccessManager accessManager;
    private ArrayDeque<SelfNaming> mAnalyseProcesses = new ArrayDeque<>(NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP);

    @PostConstruct
    public void iniMBeansSlidingWindowWithEmptySlot() {
        for (int i = 0; i < NUMBER_OF_SUBSEQUENT_PROCESS_MBEAN_TO_KEEP; i++) {
            EmptySlot emptySlot = new EmptySlot(i);
            mAnalyseProcesses.addFirst(emptySlot);
            try {
                mBeanExporter.registerManagedResource(emptySlot, emptySlot.getObjectName());
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
    }

    @ApiOperation(
        value = "Get record links",
        notes = "",
        nickname = "getRecordLinks")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
            value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
            value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
            value = "Sorting criteria in the format: property(,asc|desc). " +
                "Default sort order is ascending. ")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('UserAdmin')")
    public Page<Link> getRecordLinks(
        @ApiParam(value = "Filter, e.g. \"{url: 'png', lastState: 'ko', records: 'e421', groupId: 12}\", lastState being 'ok'/'ko'/'unknown'", required = false)
        @RequestParam(required = false) JSONObject filter,
        @ApiParam(value = "Optional, filter links to records published in that group.", required = false)
        @RequestParam(required = false) Integer[] groupIdFilter,
        @ApiParam(value = "Optional, filter links to records created in that group.", required = false)
        @RequestParam(required = false) Integer[] groupOwnerIdFilter,
        @ApiParam(value = "Optional, only links from no md.", required = false)
        @RequestParam(required = false, defaultValue = "false") Boolean orphanLink,
        @ApiIgnore Pageable pageRequest,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpSession session,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletRequest request) throws Exception {

        final UserSession userSession = ApiUtils.getUserSession(session);
        return getLinks(filter, groupIdFilter, groupOwnerIdFilter, pageRequest, userSession, request.getRemoteAddr(), orphanLink);
    }

    @ApiOperation(
        value = "Get record links as CSV",
        notes = "",
        nickname = "getRecordLinksAsCSV")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
            value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
            value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
            value = "Sorting criteria in the format: property(,asc|desc). " +
                "Default sort order is ascending. ")
    })
    @RequestMapping(
        path = "/csv",
        method = RequestMethod.GET,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public void getRecordLinksAsCsv(
        @ApiParam(value = "Filter, e.g. \"{url: 'png', lastState: 'ko', records: 'e421', groupId: 12}\", lastState being 'ok'/'ko'/'unknown'", required = false) @RequestParam(required = false) JSONObject filter,
        @ApiParam(value = "Optional, filter links to records published in that group.", required = false)
        @RequestParam(required = false) Integer[] groupIdFilter,
        @ApiParam(value = "Optional, filter links to records created in that group.", required = false)
        @RequestParam(required = false) Integer[] groupOwnerIdFilter,
        @ApiParam(value = "Optional, only links from no md.", required = false)
        @RequestParam(required = false, defaultValue = "false") Boolean orphanLink,
        @ApiIgnore
            Pageable pageRequest,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpSession session,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletResponse response,
        @ApiParam(hidden = true)
        @ApiIgnore
            HttpServletRequest request) throws Exception {
        final UserSession userSession = ApiUtils.getUserSession(session);

        final Page<Link> links = getLinks(filter, groupIdFilter, groupOwnerIdFilter, pageRequest, userSession, request.getRemoteAddr(), orphanLink);
        response.setHeader("Content-disposition", "attachment; filename=links.csv");
        LinkAnalysisReport.create(links, response.getWriter());
    }

    @ApiOperation(
        value = "Analyze records links",
        notes = "One of uuids or bucket parameter is required if not an Administrator. Only records that you can edit will be validated.",
        nickname = "analyzeRecordLinks")
    @RequestMapping(
        consumes =  MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.POST)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public SimpleMetadataProcessingReport analyzeRecordLinks(
        @ApiParam(value = API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false,
            example = "")
        @RequestBody(required = false)
            List<String> uuids,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @ApiParam(
            value = "Only allowed if Administrator or User Admin."
        )
        @RequestParam(
            required = false,
            defaultValue = "false")
            boolean analyze,
        @ApiParam(
                value = "Analyse all."
        )
        @RequestParam(
                required = false,
                defaultValue = "false")
                boolean all,
        @ApiIgnore
            HttpSession httpSession,
        @ApiIgnore
            HttpServletRequest request
    ) {
        UserSession session = ApiUtils.getUserSession(httpSession);

        SimpleMetadataProcessingReport report = new SimpleMetadataProcessingReport();

        if ((uuids != null && uuids.size() > 0)  || StringUtils.isNotEmpty(bucket) || all) {
            List<Integer> recordsList;
            try {

                if (all) {
                    if (session.getProfile() == Profile.Administrator) {
                        recordsList = metadataRepository.findAll().stream().map(Metadata::getId).collect(Collectors.toList());
                    } else {
                        recordsList = metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByOneOfFollowingGroups(new ArrayList<>(getUserGroup(session, null)))).stream().map(Metadata::getId).collect(Collectors.toList());
                    }
                } else {
                    Set<String> recordSet = ApiUtils.getUuidsParameterOrSelection(uuids.size() > 0 ? uuids.toArray(new String[uuids.size()]) : null, bucket, session);
                    recordsList = new ArrayList<>();
                    for (String uuid : recordSet){
                        if (!metadataUtils.existsMetadataUuid(uuid)) {
                            report.incrementNullRecords();
                        } else {
                            try {
                                AbstractMetadata record = ApiUtils.canViewRecord(uuid, request);
                                recordsList.add(record.getId());
                                report.addMetadataId(record.getId());
                                report.incrementProcessedRecords();
                            } catch (SecurityException e) {
                                AbstractMetadata record = metadataRepository.findOneByUuid(uuid);
                                report.addNotFoundMetadataId(record.getId());
                            }
                        }
                    }
                }
                new Thread() {
                    public void run() {
                        try {
                            getRegistredMAnalyseProcess().processMetadataAndTestLink(analyze, recordsList);
                        } catch (JDOMException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch (Exception e) {
                report.addError(e);
            } finally{
            report.close();
            }
        }
        return report;
    }

    @ApiOperation(
        value = "Remove selected links and status history (if link from no md)",
        notes = "",
        nickname = "purgeAll")
    @RequestMapping(
        path = "/del",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('Administrator')")
    @ResponseBody
    public ResponseEntity purgeSelected(
            @ApiParam(value = "One or more link ids",
                    required = true,
                    example = "")
            @RequestParam(required = false)
            Integer[] ids) {
            for (Integer id : ids) {
                urlAnalyser.deleteLink(id);
            }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private MAnalyseProcess getRegistredMAnalyseProcess() {
        MAnalyseProcess mAnalyseProcess = new MAnalyseProcess(linkRepository, metadataRepository, urlAnalyser, appContext);
        mBeanExporter.registerManagedResource(mAnalyseProcess, mAnalyseProcess.getObjectName());
        try {
            mBeanExporter.unregisterManagedResource(mAnalyseProcesses.removeLast().getObjectName());
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        mAnalyseProcesses.addFirst(mAnalyseProcess);
        return mAnalyseProcess;
    }

    private Page<Link> getLinks(
            JSONObject filter,
            Integer[] groupIdFilter,
            Integer[] groupOwnerIdFilter,
            Pageable pageRequest,
            UserSession userSession,
            String remoteAdress,
            boolean orphanLink) throws JSONException {

        Integer stateToMatch = null;
        String url = null;
        String associatedRecord = null;
        Integer[] linkFromMdWhoseGroupOwnerInFilter;
        Integer[] linkFromMdPublishedInGroupFilter;
        boolean groupOwnerIdFilterSet = groupOwnerIdFilter != null && groupOwnerIdFilter.length != 0;
        boolean groupIdFilterSet = groupIdFilter != null && groupIdFilter.length != 0;

        if (userSession.getProfile() == Profile.Administrator) {
            linkFromMdWhoseGroupOwnerInFilter = groupOwnerIdFilterSet ? groupOwnerIdFilter : null;
        } else {
            Set<Integer> userGroups = getUserGroup(userSession, remoteAdress);
            if (groupOwnerIdFilterSet) {
                userGroups.retainAll(Arrays.asList(groupOwnerIdFilter));
            }
            linkFromMdWhoseGroupOwnerInFilter = userGroups.stream().toArray(Integer[]::new);
        }

        if (userSession.getProfile() == Profile.Administrator) {
            linkFromMdPublishedInGroupFilter = groupIdFilterSet ? groupIdFilter : null;
        } else {
            Set<Integer> userGroups = getUserGroup(userSession, remoteAdress);
            if (groupIdFilterSet) {
                userGroups.retainAll(Arrays.asList(groupIdFilter));
            }
            linkFromMdPublishedInGroupFilter = userGroups.stream().toArray(Integer[]::new);
        }

        if (linkFromMdPublishedInGroupFilter != null && linkFromMdPublishedInGroupFilter.length ==0) {
            return new PageImpl<>(Collections.emptyList(), null, 0);
        }

        if (linkFromMdWhoseGroupOwnerInFilter != null && linkFromMdWhoseGroupOwnerInFilter.length ==0) {
            return new PageImpl<>(Collections.emptyList(), null, 0);
        }

        if (filter != null) {
            if (filter.has("lastState")) {
                stateToMatch = 0;
                if (filter.getString("lastState").equalsIgnoreCase("ok")) {
                    stateToMatch = 1;
                } else if (filter.getString("lastState").equalsIgnoreCase("ko")) {
                    stateToMatch = -1;
                }
            }

            if (filter.has("url")) {
                url = filter.getString("url");
            }

            if (filter.has("records")) {
                associatedRecord = filter.getString("records");
            }
        }

        if (linkFromMdPublishedInGroupFilter != null || linkFromMdWhoseGroupOwnerInFilter != null || url != null || associatedRecord != null || stateToMatch != null || orphanLink) {
            return linkRepository.findAll(LinkSpecs.filter(url, stateToMatch, associatedRecord, linkFromMdPublishedInGroupFilter, linkFromMdWhoseGroupOwnerInFilter, orphanLink), pageRequest);
        } else {
            return linkRepository.findAll(pageRequest);
        }
   }

    private Set<Integer> getUserGroup(UserSession userSession, String remoteAdress) {
        try {
            return accessManager.getUserGroups(userSession, remoteAdress, false);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
