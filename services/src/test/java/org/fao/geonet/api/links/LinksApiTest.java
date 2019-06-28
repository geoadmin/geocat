/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.links;

import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.UUID;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LinksApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private MetadataLinkRepository metadataLinkRepository;

    @Autowired
    private SchemaManager schemaManager;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SourceRepository sourceRepository;

    @PersistenceContext
    private EntityManager _entityManager;

    @Autowired
    private IMetadataUtils metadataRepository;

    private String uuid;
    private int id;
    private AbstractMetadata md;
    private MockMvc mockMvc;
    private ServiceContext context;


    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);

        final Element sampleMetadataXml = getSampleMetadataXml();
        this.uuid = UUID.randomUUID().toString();
        Xml.selectElement(sampleMetadataXml, "gmd:fileIdentifier/gco:CharacterString", Arrays.asList(GMD, GCO)).setText(this.uuid);

        String source = sourceRepository.findAll().get(0).getUuid();
        String schema = schemaManager.autodetectSchema(sampleMetadataXml);
        final Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml).setUuid(uuid);
        metadata.getDataInfo().setRoot(sampleMetadataXml.getQualifiedName()).setSchemaId(schema).setType(MetadataType.METADATA);
        metadata.getDataInfo().setPopularity(1000);
        metadata.getSourceInfo().setOwner(1).setSourceId(source);
        metadata.getHarvestInfo().setHarvested(false);


        this.id = dataManager.insertMetadata(context, metadata, sampleMetadataXml, false, false, false, UpdateDatestamp.NO,
            false, false).getId();


        dataManager.indexMetadata(Lists.newArrayList("" + this.id));
        this.md = metadataRepository.findOne(this.id);
    }

    @Test
    public void getLinks() throws Exception {
        Long operationsCount = linkRepository.count();
        final MockHttpSession httpSession = this.loginAsAdmin();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(post("/api/records/links?uuid=" + this.uuid)
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated());

        Assert.assertEquals(1, linkRepository.count());

        this.mockMvc.perform(get("/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].url").value(equalTo("http://services.sandre.eaufrance.fr/geo/ouvrage")))
            // FIXME: Should return one metadata related to that URL. 
            .andExpect(jsonPath("$[0].records", hasSize(1)))
            .andExpect(jsonPath("$[0].records[0].id.metadataId").value(equalTo(this.id)));

        this.mockMvc.perform(delete("/api/records/links")
            .session(httpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());

        Assert.assertEquals(0, linkRepository.count());
    }
}
