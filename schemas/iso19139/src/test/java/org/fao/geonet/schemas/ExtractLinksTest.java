package org.fao.geonet.schemas;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILink;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.IMetadataLink;
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

    class TestMetadata {
    }

    class TestLink implements ILink {
        public String url;

        @Override
        public ILink setUrl(String url) {
            this.url = url;
            return this;
        }
    }

    class TestMetadataLink implements IMetadataLink<TestLink, TestMetadata> {
        public TestMetadata testMetadata;
        public TestLink testLink;

        @Override
        public TestMetadataLink setId(TestMetadata testMetadata, TestLink testLink) {
            this.testLink = testLink;
            this.testMetadata = testMetadata;
            return this;
        }
    }

    ;

    RawLinkPatternStreamer<TestLink, TestMetadata, TestMetadataLink> urlRegex = new RawLinkPatternStreamer<TestLink, TestMetadata, TestMetadataLink>() {
        @Override
        protected TestLink buildLink() {
            return new TestLink();
        }

        @Override
        protected TestMetadataLink buildMetadataLink() {
            return new TestMetadataLink();
        }
    };

    @Test
    public void urlEncounteredProcessingAMetadata() throws Exception {
        TestMetadata metadata = new TestMetadata();
        urlRegex.setMetadata(metadata);

        Element inputElement = Xml.loadFile(xmlFile);
        List<Element> encounteredLinks = (List<Element>) Xml.selectNodes(inputElement, ".//gco:CharacterString", ISO19139SchemaPlugin.allNamespaces.asList());

        List<TestMetadataLink> encounteredUrl = encounteredLinks
                .stream()
                .flatMap(urlRegex::results).collect(Collectors.toList());

        assertEquals("HTTPS://acme.de/", encounteredUrl.get(0).testLink.url);
        assertEquals("ftp://mon-site.mondomaine/mon-repertoire", encounteredUrl.get(1).testLink.url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif", encounteredUrl.get(2).testLink.url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif", encounteredUrl.get(3).testLink.url);
        assertEquals(metadata, encounteredUrl.get(0).testMetadata);
        assertEquals(metadata, encounteredUrl.get(1).testMetadata);
        assertEquals(metadata, encounteredUrl.get(2).testMetadata);
        assertEquals(metadata, encounteredUrl.get(3).testMetadata);
    }
}






