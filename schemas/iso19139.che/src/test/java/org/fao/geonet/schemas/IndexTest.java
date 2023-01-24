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
import com.google.common.collect.ImmutableSet;
import net.sf.saxon.FeatureKeys;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.nio.NioPathHolder;
import org.fao.geonet.utils.nio.PathStreamSource;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.transform.JDOMResult;
import org.jdom.xpath.XPath;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(XslUtil.class)
public class IndexTest {

    static private Path PATH_TO_XSL;

    static private ImmutableList<Namespace> ALL_NAMESPACES = ImmutableSet .<Namespace>builder()
            .add(ISO19139Namespaces.GCO)
            .add(ISO19139Namespaces.GMD)
            .add(ISO19139Namespaces.SRV)
            .add(ISO19139Namespaces.XSI)
            .add(ISO19139cheNamespaces.CHE)
            .build().asList();

    public IndexTest() {
        PowerMockito.mockStatic(XslUtil.class);
        PowerMockito.when(XslUtil.getSettingValue(eq("system/metadata/validation/removeSchemaLocation"))).thenReturn("false");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("ger"))).thenReturn("DE");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("ita"))).thenReturn("IT");
        PowerMockito.when(XslUtil.twoCharLangCode(eq("fre"))).thenReturn("FR");
    }

    @BeforeClass
    public static void setup() throws URISyntaxException {
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        PATH_TO_XSL = Paths.get(IndexTest.class.getClassLoader().getResource("iso19139.che/index-fields/index.xsl").toURI());
    }

    @Test
    public void index() throws Exception {
        NioPathHolder.setBase(PATH_TO_XSL);
        TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();
        InputStream in = IO.newInputStream(PATH_TO_XSL);
        Source srcSheet = new StreamSource(in, PATH_TO_XSL.toFile().toURI().toASCIIString());
        transFact.setURIResolver(new URIResolver() {
            @Override
            public Source resolve(String path, String base) throws TransformerException {
                return new PathStreamSource(Paths.get(IndexTest.class.getClassLoader().getResource(".").getPath()).resolve(path.replace("../", "" )));
            }
        });

        boolean debug = false;
        if (debug) {
            Class<?> factoryClass = transFact.getClass();
            ClassLoader loader = factoryClass.getClassLoader();
            Class<?> messageWarner = loader.loadClass("net.sf.saxon.event.MessageWarner");
            transFact.setAttribute(FeatureKeys.MESSAGE_EMITTER_CLASS, messageWarner.getName());
        }

        Transformer t = transFact.newTransformer(srcSheet);
        JDOMResult resXml = new JDOMResult();
        t.transform(new StreamSource(IndexTest.class.getClassLoader().getResourceAsStream("call_112.xml")), resXml);

        XPath xpath = XPath.newInstance("/doc/pointOfContactOrgForResourceObject");
        Element orgForResourceObject = (Element) xpath.selectNodes(resXml.getDocument()).get(0);
        orgForResourceObject.getContent().get(0);
        String test = ((Text) (orgForResourceObject.getContent().get(0))).getText().replaceAll("\n[ ]*\\{", "{");
        assertTrue(test.contains("\"langger\":\"Bundesamt für Kommunikation\""));
        assertTrue(test.contains("\"default\":\"Bundesamt für Kommunikation\""));
        assertTrue(test.contains("\"langfre\":\"Office fédéral de la communication\""));
        JSONObject valid = new JSONObject(test);
    }
}
