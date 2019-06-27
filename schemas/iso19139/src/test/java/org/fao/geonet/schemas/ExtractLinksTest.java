package org.fao.geonet.schemas;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ExtractLinksTest extends XslProcessTest {

    public ExtractLinksTest() {
        super();
        this.setXmlFilename("schemas/xsl/process/input_with_url.xml");
        this.setNs(ISO19139SchemaPlugin.allNamespaces);
    }

    class TestLink  {
        public String url;

        public TestLink setUrl(String url) {
            this.url = url;
            return this;
        }
    }


    RawLinkPatternStreamer<TestLink> urlRegex = new RawLinkPatternStreamer(new ILinkBuilder<TestLink>() {
        @Override
        public TestLink build() {
            return new TestLink();
        }

        @Override
        public void setUrl(TestLink link, String url) {
            link.setUrl(url);
        }
    });

    @Test
    public void urlEncounteredProcessingAMetadata() throws Exception {

        Element inputElement = Xml.loadFile(xmlFile);
        List<Element> encounteredLinks = (List<Element>) Xml.selectNodes(inputElement, ".//gco:CharacterString", ISO19139SchemaPlugin.allNamespaces.asList());

        List<TestLink> encounteredUrl = encounteredLinks
                .stream()
                .flatMap(urlRegex::results).collect(Collectors.toList());

        assertEquals("HTTPS://acme.de/", encounteredUrl.get(0).url);
        assertEquals("ftp://mon-site.mondomaine/mon-repertoire", encounteredUrl.get(1).url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif", encounteredUrl.get(2).url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif", encounteredUrl.get(3).url);

    }
}






