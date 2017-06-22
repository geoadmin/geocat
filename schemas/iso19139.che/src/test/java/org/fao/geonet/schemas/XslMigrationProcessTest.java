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

import org.fao.geonet.schema.iso19139che.ISO19139cheSchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

@Ignore
public class XslMigrationProcessTest extends XslProcessTest {

    public XslMigrationProcessTest() {
        super();
        this.setXslFilename("../../../../../../../iso19139.che/src/main/plugin/iso19139.che/process/migration3_4.xsl");
        this.setXmlFilename("dataset.xml");
        this.setNs(ISO19139cheSchemaPlugin.allNamespaces);
    }

    @Test
    public void testDatasetMigration() throws Exception {

        Element inputElement = Xml.loadFile(
            root.resolve("xsl/process/dataset.xml"));

        String resultString = Xml.getString(inputElement);

        // No schema location anymore.
        assertThat(
            resultString, hasXPath(
                "count(//@xsi:schemaLocation)",
                equalTo("0")).withNamespaceContext(ns)
        );


        // No characterString in language, but languageCode.
        assertThat(
            resultString, hasXPath(
                "count(//gmd:language[gco:CharacterString])",
                equalTo("0")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath(
                "count(//gmd:language[gmd:LanguageCode])",
                equalTo("1")).withNamespaceContext(ns)
        );


        // Parent identifier in aggregates now.
        assertThat(
            resultString, hasXPath(
                "count(//gmd:parentIdentifier)",
                equalTo("1")).withNamespaceContext(ns)
        );
        assertThat(
            resultString, hasXPath(
                "//gmd:identificationInfo/che:CHE_MD_DataIdentification/" +
                    "gmd:aggregationInfo/gmd:MD_AggregateInformation/" +
                        "gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/" +
                            "gmd:code/gco:CharacterString",
                equalTo("aee4fe79-a583-46a1-bf46-19ee613ce415"))
                    .withNamespaceContext(ns)
        );
    }
}
