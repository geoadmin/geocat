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

import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(XslUtil.class)
public class UpdateFixedInfoTest {

    static private Path PATH_TO_XSL;

    @BeforeClass
    public static void setup() throws URISyntaxException {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        PATH_TO_XSL = Paths.get(UpdateFixedInfoTest.class.getClassLoader().getResource("iso19139.che/update-fixed-info.xsl").toURI());
        PowerMockito.mockStatic(XslUtil.class);
        PowerMockito.when(XslUtil.getSettingValue(eq("system/metadata/validation/removeSchemaLocation"))).thenReturn("false");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("ger"))).thenReturn("DE");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("ita"))).thenReturn("IT");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("fre"))).thenReturn("FR");
    }

    @Test
    public void dataLetUnchanged() throws Exception {
        Diff diff = compareOutputwithExpected(
                "ufi/multilingual_unchanged.xml",
                new ExpectedFromInput() {
                    @Override
                    public Element invoke(Element input) {
                        return input.getChild("CHE_MD_Metadata", ISO19139cheNamespaces.CHE);
                    }
                });

        assertFalse("Process does not alter the document.", diff.hasDifferences());
    }

    private Diff compareOutputwithExpected(String inputName, ExpectedFromInput expectedFromInput) throws Exception {
        Element input = Xml.loadFile(Paths.get(UpdateFixedInfoTest.class.getClassLoader().getResource(inputName).toURI()));
        String processed = Xml.getString( Xml.transform(input, PATH_TO_XSL));
        String expected = Xml.getString(expectedFromInput.invoke(input));
        return DiffBuilder
                .compare(Input.fromString(processed))
                .withTest(Input.fromString(expected))
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .checkForSimilar()
                .build();
    }

    interface ExpectedFromInput {
        Element invoke(Element input);
    }
}
