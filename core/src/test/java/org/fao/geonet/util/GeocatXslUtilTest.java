package org.fao.geonet.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jeeves.xlink.XLink;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jesse on 4/29/2015.
 */
public class GeocatXslUtilTest {

    @Test
    public void testMergeKeywordsSplit() throws Exception {
        Element xml = Xml.loadStream(GeocatXslUtilTest.class.getResourceAsStream("keywords.xml"));

        GeocatXslUtil.mergeKeywords(xml, true, null, null);

        List<Element> nodes = (List<Element>) Xml.selectNodes(xml, "*/gmd:descriptiveKeywords");
        assertEquals(2, nodes.size());

        Multimap<String, String> thesaurusToIds = parseOutKeywords(nodes);

        assertEquals(3, thesaurusToIds.get("external._none_.gemet").size());
        assertEquals(1, thesaurusToIds.get("external.theme.inspire-service-taxonomy").size());
    }

    @Test
    public void testMergeKeywordsMerge() throws Exception {
        Element xml = Xml.loadStream(GeocatXslUtilTest.class.getResourceAsStream("merge-keywords.xml"));

        List<Element> nodes = (List<Element>) Xml.selectNodes(xml, "*/gmd:descriptiveKeywords");
        Multimap<String, String> thesaurusToIds = parseOutKeywords(nodes);
        assertEquals(0, thesaurusToIds.get("external._none_.gemet").size());
        assertEquals(5, thesaurusToIds.get("local._none_.geocat.ch").size());
        assertEquals(1, thesaurusToIds.get("external.theme.inspire-service-taxonomy").size());

        GeocatXslUtil.mergeKeywords(xml, true, null, null);

        nodes = (List<Element>) Xml.selectNodes(xml, "*/gmd:descriptiveKeywords");
        assertEquals(2, nodes.size());

        thesaurusToIds = parseOutKeywords(nodes);

        assertEquals(0, thesaurusToIds.get("external._none_.gemet").size());
        assertEquals(9, thesaurusToIds.get("local._none_.geocat.ch").size());
        assertEquals(2, thesaurusToIds.get("external.theme.inspire-service-taxonomy").size());
    }

    private Multimap<String, String> parseOutKeywords(List<Element> nodes) {
        Multimap<String, String> thesaurusToIds = HashMultimap.create();

        for (Element keyword : nodes) {
            String attributeValue = keyword.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK);
            Matcher idmatcher = Pattern.compile("id=([^&]+)").matcher(attributeValue);
            Matcher thesmatcher = Pattern.compile(".*thesaurus=([^&]+).*").matcher(attributeValue);
            assertTrue(idmatcher.find());
            assertTrue(thesmatcher.find());
            String ids = idmatcher.group(1);
            for (String id : ids.split(",")) {
                thesaurusToIds.put(thesmatcher.group(1), id);
            }
        }
        return thesaurusToIds;
    }

    @Test
    public void testMergeKeywordsNoKeywords() throws Exception {
        Element xml = Xml.loadStream(GeocatXslUtilTest.class.getResourceAsStream("no-keywords.xml"));

        GeocatXslUtil.mergeKeywords(xml, true, null, null);

        List<Element> nodes = (List<Element>) Xml.selectNodes(xml, "*/gmd:descriptiveKeywords");
        assertEquals(0, nodes.size());
    }
}