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

package org.fao.geonet.schemas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheSchemaPlugin;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

@RunWith(PowerMockRunner.class)
@PrepareForTest(XslUtil.class)
public class InspireMigrationProcessTest {

    static private Path PATH_TO_XSL;

    static private ImmutableSet<Namespace> ALL_NAMESPACES = ImmutableSet.<Namespace>builder()
        .add(ISO19139Namespaces.GCO)
        .add(ISO19139Namespaces.GMD)
        .add(ISO19139Namespaces.SRV)
        .add(ISO19139Namespaces.GMX)
        .add(ISO19139Namespaces.XSI)
        .add(ISO19139Namespaces.XLINK)
        .add(ISO19139cheNamespaces.CHE)
        .build();
    static private ImmutableMap<String, String> ALL_NS = ImmutableMap.<String, String>builder()
        .put(ISO19139Namespaces.XLINK.getPrefix(), ISO19139Namespaces.XLINK.getURI())
        .put(ISO19139Namespaces.GCO.getPrefix(), ISO19139Namespaces.GCO.getURI())
        .put(ISO19139Namespaces.GMD.getPrefix(), ISO19139Namespaces.GMD.getURI())
        .put(ISO19139Namespaces.GMX.getPrefix(), ISO19139Namespaces.GMX.getURI())
        .put(ISO19139Namespaces.SRV.getPrefix(), ISO19139Namespaces.SRV.getURI())
        .put(ISO19139cheNamespaces.CHE.getPrefix(), ISO19139cheNamespaces.CHE.getURI())
        .build();
    public InspireMigrationProcessTest() {
    }

    @BeforeClass
    public static void setup() throws URISyntaxException {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        PATH_TO_XSL = Paths.get(InspireMigrationProcessTest.class.getClassLoader().getResource("iso19139.che/process/geocat-inspire-conformity.xsl").toURI());

    }

    @Test
    public void testDataset() throws Exception {
        Element input = Xml.loadFile(Paths.get(InspireMigrationProcessTest.class.getClassLoader().getResource("inspiredataset.xml").toURI()));
        String inputString = Xml.getString(input);


        // Metadata / Distributor instead of pointOfContact
        Element output = Xml.transform(input, PATH_TO_XSL);
        String outputString = Xml.getString(output);
        assertThat(
            inputString, hasXPath(
                "count(//gmd:contact)",
                equalTo("2")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            inputString, hasXPath(
                "count(//gmd:contact[contains(@xlink:href, '~distributor')])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            inputString, hasXPath(
                "count(//gmd:contact[contains(@xlink:href, '~principalInvestigator')])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );

        assertThat(
            outputString, hasXPath(
                "count(//gmd:contact[contains(@xlink:href, '~pointOfContact')])",
                equalTo("2")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//gmd:contact[contains(@xlink:href, '~distributor') or contains(@xlink:href, '~principalInvestigator')])",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "//gmd:contact[1]/@xlink:href",
                equalTo("local://srv/api/registries/entries/d8d920c8-cc8c-47b7-ba1b-2b039ad0f4b3?process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact&lang=fre,ger,ita,eng,roh")).withNamespaceContext(ALL_NS)
        );



        // Resource / Responsible organization missing
        assertThat(
            inputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact)",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact)",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/@xlink:href",
                equalTo("local://srv/api/registries/entries/d8d920c8-cc8c-47b7-ba1b-2b039ad0f4b3?process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact&lang=fre,ger,ita,eng,roh")).withNamespaceContext(ALL_NS)
        );


        // Resource / Identification / Temporal reference / LastRevision
        assertThat(
            inputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:dateType/*/@codeListValue[. = 'lastRevision'])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:dateType/*/@codeListValue[. = 'revision'])",
                equalTo("2")).withNamespaceContext(ALL_NS)
        );


        // Resource / Quality / Lineage
        assertThat(
            inputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:dataQualityInfo)",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:dataQualityInfo)",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );

        // Resource / Conformity missing
        assertThat(
            inputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title[gmx:Anchor/@xlink:href = 'http://data.europa.eu/eli/reg/2010/1089'])",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(/che:CHE_MD_Metadata/gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title[gmx:Anchor/@xlink:href = 'http://data.europa.eu/eli/reg/2010/1089'])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );

        // Resource / Access constraint / Limitation on public access AND condition to access and use
        assertThat(
            inputString, hasXPath(
                "count(//gmd:resourceConstraints[count(*) = 0])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//gmd:resourceConstraints[count(*) = 0])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//gmd:resourceConstraints[.//*/@codeListValue='restricted'])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );


    }


    @Test
    public void testDatasetWithNoResourceIdentifierNoCRS() throws Exception {
        Element input = Xml.loadFile(Paths.get(InspireMigrationProcessTest.class.getClassLoader().getResource("inspiredataset-noresourceidentifier.xml").toURI()));
        String inputString = Xml.getString(input);


        // Dataset / No resource identifier
        Element output = Xml.transform(input, PATH_TO_XSL);
        String outputString = Xml.getString(output);
        assertThat(
            inputString, hasXPath(
                "count(//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier)",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/*/text()",
                equalTo("basicGeodataID:166.1")).withNamespaceContext(ALL_NS)
        );


        // Dataset / No CRS
        assertThat(
            inputString, hasXPath(
                "count(//gmd:referenceSystemInfo)",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//gmd:referenceSystemInfo)",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );



        // Resource / Access constraint / Limitation on public access AND condition to access and use
        assertThat(
            outputString, hasXPath(
                "count(//gmd:resourceConstraints)",
                equalTo("2")).withNamespaceContext(ALL_NS)
        );


        // Dataset / No INSPIRE theme
        assertThat(
            outputString, hasXPath(
                "//gmd:descriptiveKeywords[contains(@xlink:href, 'httpinspireeceuropaeutheme')]/@xlink:href",
                equalTo("local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&thesaurus=external.theme.httpinspireeceuropaeutheme-theme&id=http%3A%2F%2Finspire.ec.europa.eu%2Ftheme%2Fge,http%3A%2F%2Finspire.ec.europa.eu%2Ftheme%2Fso,http%3A%2F%2Finspire.ec.europa.eu%2Ftheme%2Fnz&lang=fre")).withNamespaceContext(ALL_NS)
        );
    }


    @Test
    public void testService() throws Exception {
        Element input = Xml.loadFile(Paths.get(InspireMigrationProcessTest.class.getClassLoader().getResource("inspireservice.xml").toURI()));
        String inputString = Xml.getString(input);


        // Service / Spatial data service type
        Element output = Xml.transform(input, PATH_TO_XSL);
        String outputString = Xml.getString(output);
        assertThat(
            inputString, hasXPath(
                "count(//srv:serviceType[* = 'OGC:WFS'])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//srv:serviceType[* = 'OGC:WFS'])",
                equalTo("0")).withNamespaceContext(ALL_NS)
        );
        assertThat(
            outputString, hasXPath(
                "count(//srv:serviceType[* = 'download'])",
                equalTo("1")).withNamespaceContext(ALL_NS)
        );

        // Service / Spatial data service category
        assertThat(
            outputString, hasXPath(
                "//gmd:descriptiveKeywords[contains(@xlink:href, 'httpinspireeceuropaeumetadatacodelistSpatialDataServiceCategory')]/@xlink:href",
                equalTo("local://srv/api/registries/vocabularies/keyword?skipdescriptivekeywords=true&thesaurus=external.theme.httpinspireeceuropaeumetadatacodelistSpatialDataServiceCategory-SpatialDataServiceCategory&id=http%3A%2F%2Finspire.ec.europa.eu%2Fmetadata-codelist%2FSpatialDataServiceCategory%2FinfoProductAccessService&lang=ger,fre,eng,ita&transformation=to-iso19139-keyword-with-anchor")).withNamespaceContext(ALL_NS)
        );
    }
}
