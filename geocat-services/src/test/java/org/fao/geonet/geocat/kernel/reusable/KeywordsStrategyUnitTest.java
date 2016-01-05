package org.fao.geonet.geocat.kernel.reusable;

import jeeves.xlink.XLink;
import org.jdom.Element;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KeywordsStrategyUnitTest {
    @Test
    public void testIndexLinks() throws UnsupportedEncodingException {
        Collection<String> xLinks = createXLinks();
        Map<String, String[]> index = KeywordsStrategy.indexLinks(xLinks);
        assertEquals(2, index.size());
        assertTrue(index.containsKey("local._none_.non_validated"));
        assertTrue(index.containsKey("local._none_.geocat.ch"));
        assertArrayEquals(new String[] {"http://custom.shared.obj.ch/concept#foo",
                "http://custom.shared.obj.ch/concept#b4ef6187-2a69-4bb2-ae3e-4bc04a25211e"},
                index.get("local._none_.non_validated"));
        assertArrayEquals(new String[] {"http://custom.shared.obj.ch/concept#toto"},
                index.get("local._none_.geocat.ch"));
    }

    @Test
    public void testUpdateXLinksSimple() throws UnsupportedEncodingException {
        Path appPath = FileSystems.getDefault().getPath("toto");
        KeywordsStrategy strategy = new KeywordsStrategy(null, null, appPath, null, null);
        Collection<String> xlinks = new ArrayList<>();
        xlinks.add("local://xml.keyword.get?thesaurus=local._none_.non_validated&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23foo&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords");
        Element xml = new Element("CHE_MD_Metadata", "http://www.geocat.ch/2008/che");
        final Element ident = new Element("CHE_MD_DataIdentification", "http://www.geocat.ch/2008/che");
        xml.addContent(ident);
        final Element nonValid = strategy.xlinkIt("local._none_.non_validated", "http://custom.shared.obj.ch/concept#foo", true);
        ident.addContent(nonValid);

        strategy.updateXLinks("thesaurus=local._none_.non_validated&id=http://custom.shared.obj.ch/concept#foo",
                "thesaurus=local._none_.geocat.ch&id=http://custom.shared.obj.ch/concept#foo", xlinks, xml);

        assertEquals("CHE_MD_Metadata", xml.getName());
        assertEquals(1, xml.getContentSize());
        Element actualIdent = (Element) xml.getContent(0);
        assertEquals("CHE_MD_DataIdentification", actualIdent.getName());
        assertEquals(1, actualIdent.getContentSize());

        Element xlink = (Element) actualIdent.getContent(0);
        assertEquals("descriptiveKeywords", xlink.getName());
        assertEquals("local://xml.keyword.get?thesaurus=local._none_.geocat.ch&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23foo&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords",
                xlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));
    }

    @Test
    public void testUpdateXLinksMultiple() throws UnsupportedEncodingException {
        Path appPath = FileSystems.getDefault().getPath("toto");
        KeywordsStrategy strategy = new KeywordsStrategy(null, null, appPath, null, null);
        Collection<String> xlinks = createXLinks();
        Element xml = new Element("CHE_MD_Metadata", "http://www.geocat.ch/2008/che");
        final Element ident = new Element("CHE_MD_DataIdentification", "http://www.geocat.ch/2008/che");
        xml.addContent(ident);
        final Element nonValid = strategy.xlinkIt("local._none_.non_validated",
                "http://custom.shared.obj.ch/concept#foo,http://custom.shared.obj.ch/concept#b4ef6187-2a69-4bb2-ae3e-4bc04a25211e", true);
        ident.addContent(nonValid);
        final Element valid = strategy.xlinkIt("local._none_.geocat.ch",
                "http://custom.shared.obj.ch/concept#toto", true);
        ident.addContent(valid);

        strategy.updateXLinks("thesaurus=local._none_.non_validated&id=http://custom.shared.obj.ch/concept#foo",
                "thesaurus=local._none_.geocat.ch&id=http://custom.shared.obj.ch/concept#foo", xlinks, xml);

        assertEquals("CHE_MD_Metadata", xml.getName());
        assertEquals(1, xml.getContentSize());
        Element actualIdent = (Element) xml.getContent(0);
        assertEquals("CHE_MD_DataIdentification", actualIdent.getName());
        assertEquals(2, actualIdent.getContentSize());

        Element firstXlink = (Element) actualIdent.getContent(0);
        assertEquals("descriptiveKeywords", firstXlink.getName());
        Element secondXlink = (Element) actualIdent.getContent(1);
        assertEquals("descriptiveKeywords", secondXlink.getName());

        final String expectedNonValid = "local://xml.keyword.get?thesaurus=local._none_.non_validated&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23b4ef6187-2a69-4bb2-ae3e-4bc04a25211e&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords";
        final String expectedValid = "local://xml.keyword.get?thesaurus=local._none_.geocat.ch&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23toto,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23foo&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords";
        if (secondXlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK).contains("non_validated")) {
            assertEquals(expectedNonValid, secondXlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));
            assertEquals(expectedValid, firstXlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));
        } else {
            assertEquals(expectedNonValid, firstXlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));
            assertEquals(expectedValid, secondXlink.getAttributeValue(XLink.HREF, XLink.NAMESPACE_XLINK));
        }
    }

    private static Collection<String> createXLinks() {
        Collection<String> xLinks = new ArrayList<>();
        xLinks.add("local://xml.keyword.get?thesaurus=local._none_.non_validated&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23foo,http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23b4ef6187-2a69-4bb2-ae3e-4bc04a25211e&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords");
        xLinks.add("local://xml.keyword.get?thesaurus=local._none_.geocat.ch&id=http%3A%2F%2Fcustom.shared.obj.ch%2Fconcept%23toto&multiple=false&lang=fre,eng,ger,ita,roh&textgroupOnly&skipdescriptivekeywords");
        return xLinks;
    }
}
