package org.fao.geonet.schemas;

import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.RawLinkPatternStreamer;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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



    @Test
    public void urlEncounteredProcessingAMetadata() throws Exception {
        Element mdToprocess = Xml.loadFile(xmlFile);

        List<TestLink> persisted = new ArrayList<TestLink>();
        RawLinkPatternStreamer<TestLink> toTest = new RawLinkPatternStreamer(new ILinkBuilder<TestLink>() {

            @Override
            public TestLink build() {
                return new TestLink();
            }

            @Override
            public void setUrl(TestLink link, String url) {
                link.setUrl(url);
            }

            @Override
            public void persist(TestLink link) {
                persisted.add(link);
            }
        });
        toTest.setNamespaces(ISO19139SchemaPlugin.allNamespaces.asList());
        toTest.setRawTextXPath(".//gco:CharacterString");

        toTest.processAllRawText(mdToprocess);

        assertEquals("HTTPS://acme.de/", persisted.get(0).url);
        assertEquals("ftp://mon-site.mondomaine/mon-repertoire", persisted.get(1).url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail_s.gif", persisted.get(2).url);
        assertEquals("http://apps.titellus.net/geonetwork/srv/api/records/da165110-88fd-11da-a88f-000d939bc5d8/attachments/thumbnail.gif", persisted.get(3).url);
    }
}






