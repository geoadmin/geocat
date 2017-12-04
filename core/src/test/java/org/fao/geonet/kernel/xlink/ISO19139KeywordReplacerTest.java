//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.xlink;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.schema.subtemplate.Status;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by fgravin on 10/26/17.
 */
public class ISO19139KeywordReplacerTest extends AbstractCoreIntegrationTest {

    private static final String KEYWORD_RESOURCE = "kernel/vicinityKeyword.xml";

    @Autowired
    private ISO19139KeywordReplacer toTest;

    @Before
    public void initLanguageMapper() {
        IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
            {
                _isoLanguagesMap639.put("en", "eng");
                _isoLanguagesMap639.put("de", "ger");
                _isoLanguagesMap639.put("fr", "fre");
                _isoLanguagesMap639.put("it", "ita");
            }
        };
        toTest.isoLanguagesMapper = isoLangMapper;
    }

    @Test
    public void replaceAllAndTestOneXLinkHRef() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);

        Status status = toTest.replaceAll(md);

        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(5, themes.size());
        assertTrue(themes.contains("external.theme.gemet|6528"));
        assertTrue(themes.contains("external.theme.gemet|7784"));
        assertTrue(themes.contains("external.theme.gemet|6549"));
        assertTrue(themes.contains("external.place.regions|CAF"));
        assertTrue(themes.contains("external.place.regions|ECU"));




        assertEquals("local://srv/api/registries/vocabularies/keyword?thesaurus=external.theme.gemet&" +
                        "id=http%3A%2F%2Fwww.eionet.europa.eu%2Fgemet%2Fconcept%2F6549&multiple=false&" +
                        "lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords",
                ((Element)Xml.selectNodes(md, ".//gmd:descriptiveKeywords").get(0))
                        .getAttribute("href", ISO19139Namespaces.XLINK).getValue());
    }

    @Test
    public void replaceAllAvoidDuplicateForAGivenThesaurus() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        List<?> fischerei = Xml.selectNodes(md, "*//gco:CharacterString[text()='external.theme.gemet-theme']" +
                "//parent::*//parent::*//parent::*//parent::gmd:MD_Keywords" +
                "//child::gmd:keyword//child::gco:CharacterString");
        ((Element) fischerei.get(0)).setText("Fischereiwirtschaft");
        ((Element) fischerei.get(1)).setText("gestion de la pêche");
        Xml.selectElement(md,
                "*//child::gmd:LocalisedCharacterString[text()='allevamento di pollame']")
                .setText("sfruttamento razionale della pesca industriale");

        Status status = toTest.replaceAll(md);

        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(3, themes.size());
        assertTrue(themes.contains("external.theme.gemet|3238"));
        assertTrue(themes.contains("external.place.regions|CAF"));
        assertTrue(themes.contains("external.place.regions|ECU"));
    }

    @Test
    public void elementAlreadyProcessedByAnotherTranslation() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        Xml.selectElement(md,
                "*//child::gmd:LocalisedCharacterString[text()='only it translation does have a meaning']")
                .setText("poultry farming");

        Status status = toTest.replaceAll(md);

        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(5, themes.size());
        assertTrue(themes.contains("external.theme.gemet|6549"));
    }

    @Test
    public void emptyTranslation() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        Xml.selectElement(md,
                "*//child::gmd:LocalisedCharacterString[text()='only it translation does have a meaning']")
                .setText("");

        Status status = toTest.replaceAll(md);

        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(6, themes.size());
        assertTrue(themes.contains("external.theme.gemet|6549"));
    }

    @Test
    public void nullTranslation() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        Xml.selectElement(md,
                "*//child::gmd:LocalisedCharacterString[text()='only it translation does have a meaning']")
                .removeContent();

        Status status = toTest.replaceAll(md);

        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(6, themes.size());
        assertTrue(themes.contains("external.theme.gemet|6549"));
    }

    @Test
    public void xlinkHasToBeLetInPlaceNotAnError() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        List<Element> descKeywords = (List<Element>) Xml.selectNodes(md, ".//gmd:descriptiveKeywords");
        descKeywords.get(1).detach();
        Element xlinkedElem = Xml.loadString("<gmd:descriptiveKeywords xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:show=\"embed\" " +
                "xlink:href=\"local://srv/api/registries/vocabularies/keyword?thesaurus=external.place.regions&amp;" +
                "id=http%3A%2F%2Fwww.naturalearthdata.com%2Fne_admin%23Country%2FECU&amp;multiple=false&amp;" +
                "lang=fre,eng,ger,ita,roh&amp;textgroupOnly&amp;skipdescriptivekeywords\" />", false);
        ((Element)descKeywords.get(0).getParent()).addContent(xlinkedElem);
        Status status = toTest.replaceAll(md);


        assertFalse(status.isError());
        Set<String> themes = getXLinkedKeyword(md);
        assertEquals(4, themes.size());
        assertTrue(themes.contains("external.place.regions|ECU"));
    }

    @Test
    public void statusFailure() throws IOException, JDOMException {
        Element md = getSubtemplateXml(KEYWORD_RESOURCE);
        Xml.selectElement(md,
                "*//child::gco:CharacterString[text()='Soziale Bedingung']")
                .setText("I don't exist");
        Xml.selectElement(md,
                "*//child::gco:CharacterString[text()='Ecuador']")
                .setText("Nowhereland");

        String initial = Xml.getString(md);

        Status status = toTest.replaceAll(md);

        String replaced = Xml.getString(md);
        assertFalse(initial.equals(replaced));
        assertTrue(status.isError());
        assertEquals("||Incomplete match for keyword |I don't exist||Incomplete match for keyword |Nowhereland", status.getMsg());
    }

    @Test
    public void searchInAnyTheasurusGer() throws Exception {
        KeywordBean keyword = toTest.searchInAnyThesaurus("Fischerei");
        assertNotNull(keyword);
    }

    @Test
    public void searchInAnyTheasurusFre() throws Exception {
        KeywordBean keyword = toTest.searchInAnyThesaurus("pêche (général)");
        assertNotNull(keyword);
    }

    private Element getSubtemplateXml(String path) throws IOException, JDOMException {
        URL contactResource = AbstractCoreIntegrationTest.class.getResource(path);
        Element subtemplateElement = Xml.loadStream(contactResource.openStream());
        return subtemplateElement;
    }

    private Set<String> getXLinkedKeyword(Element md) throws JDOMException {
        Pattern p = Pattern.compile(".*thesaurus=(.+?)\\&.*id=.*%2F(.+?)\\&.*");
        return Xml.selectNodes(md, ".//gmd:descriptiveKeywords")
                .stream()
                .map(object -> {
                    Matcher matcher = p.matcher(((Element) object).getAttribute("href", ISO19139Namespaces.XLINK).getValue());
                    matcher.matches();
                    return String.format("%s|%s", matcher.group(1), matcher.group(2));
                })
                .collect(Collectors.toSet());
    }
}
