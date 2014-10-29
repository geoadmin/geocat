package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import jeeves.xlink.XLink;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.geocat.services.reusable.AbstractSharedObjectTest;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.schema.iso19139che.ISO19139cheSchemaPlugin;
import org.fao.geonet.services.subtemplate.Get;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_EXTRA_FIELD;
import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_EXTRA_NON_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy.LUCENE_UUID_FIELD;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces.CHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FormatsStrategyTest extends AbstractSharedObjectStrategyTest {

    @Autowired
    ReusableObjManager manager;
    @Autowired
    private MetadataRepository metadataRepository;


    @Test
    public void testNullNameAndVersion() throws Exception {

        final Metadata format = addFormatSubtemplate("testFind_NoXLink", true);
        Element xmlData = format.getXmlData(false);
        xmlData.getChild("name", GMD).getChild("CharacterString", GCO).detach();

        String noNameUUID = "NoName";
        saveSubtemplate(noNameUUID, false, xmlData);
        assertFormatProcessing(xmlData, noNameUUID);


        xmlData = format.getXmlData(false);
        xmlData.getChild("version", GMD).getChild("CharacterString", GCO).detach();
        String noVersionUUID = "NoVersion";
        saveSubtemplate(noVersionUUID, false, xmlData);
        assertFormatProcessing(xmlData, noVersionUUID);
    }

    private void assertFormatProcessing(Element xmlData, String uuid) throws Exception {
        Element md = createMetadata(xmlData);
        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md, md, false, null, context);
        final List<Element> process = manager.process(params);
        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedEl = Xml.selectElement(updated, "*//gmd:distributionFormat", Arrays.asList(GMD));
        final String href = updatedEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("uuid=" + uuid));
    }

    @Test
    public void testFind_NoXLink() throws Exception {
        final Metadata format = addFormatSubtemplate("testFind_NoXLink", true);

        final Element formatXml = format.getXmlData(false);
        Element md = createMetadata(formatXml);

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, null, context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedEl = Xml.selectElement(updated, "*//gmd:distributionFormat", Arrays.asList(GMD));
        final String href = updatedEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("uuid=" + format.getUuid()));

        LocalServiceRequest request = LocalServiceRequest.create(href);

        final Element paramXml = request.getParams();
        final Get get = new Get();

        final Element subtemplateXml = get.exec(paramXml, context);
        assertEqualsText("testFind_NoXLinkname", subtemplateXml, "gmd:name/gco:CharacterString", GMD, CHE, GCO);
        assertEqualsText("testFind_NoXLinkversion", subtemplateXml, "gmd:version/gco:CharacterString", GMD, CHE, GCO);
    }

    @Test
    public void testAdd() throws Exception {
        Element sharedObjTmp = Xml.loadFile(AbstractSharedObjectTest.class.getResource(SHARED_FORMAT_XML));
        Element md = createMetadata(sharedObjTmp);

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, null, context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedEl = Xml.selectElement(updated, "*//gmd:distributionFormat", Arrays.asList(GMD));
        final String href = updatedEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("uuid="));

        final String uuid = Utils.id(href);

        final MetadataRepository repository = _applicationContext.getBean(MetadataRepository.class);
        final Metadata oneByUuid = repository.findOneByUuid(uuid);

        assertNotNull(oneByUuid);
        assertEquals(MetadataType.SUB_TEMPLATE, oneByUuid.getDataInfo().getType());
        assertEquals("gmd:MD_Format", oneByUuid.getDataInfo().getRoot());
        assertEquals(LUCENE_EXTRA_NON_VALIDATED, oneByUuid.getDataInfo().getExtra());
        assertEquals(ISO19139cheSchemaPlugin.IDENTIFIER, oneByUuid.getDataInfo().getSchemaId());
        final Element xmlData = oneByUuid.getXmlData(false);

        assertEquals("{{uuid}}name", xmlData.getChild("name", GMD).getChildText("CharacterString", GCO));
        assertEquals("{{uuid}}version", xmlData.getChild("version", GMD).getChildText("CharacterString", GCO));

        final SearchManager searchManager = _applicationContext.getBean(SearchManager.class);
        final IndexAndTaxonomy reader = searchManager.getNewIndexReader("eng");

        try {
            final IndexSearcher searcher = new IndexSearcher(reader.indexReader);
            final TopDocs search = searcher.search(new TermQuery(new Term(LUCENE_EXTRA_FIELD, LUCENE_EXTRA_NON_VALIDATED)), 300);

            final ScoreDoc[] scoreDocs = search.scoreDocs;

            Set<String> uuids = Sets.newHashSet();
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                uuids.add(doc.get(LUCENE_UUID_FIELD));
            }

            assertEquals(1, uuids.size());
        } finally {
            searchManager.releaseIndexReader(reader);
        }
    }

    protected Metadata createDefaultSubtemplate(boolean validated) throws Exception {
        return addUserSubtemplate("format" + UUID.randomUUID(), validated);
    }

    protected String getIsValidatedSpecificData() {
        return null;
    }

    protected ReplacementStrategy createReplacementStrategy() {
        return new FormatsStrategy(_applicationContext);
    }


    private Element createMetadata(Element formatXml) {
        return new Element("CHE_MD_Metadata", CHE).addContent(
                new Element("distributionInfo", GMD).addContent(
                        new Element("MD_Distribution", GMD).addContent(
                                new Element("distributionFormat", GMD).addContent(formatXml)
                        )
                )
        );
    }
}