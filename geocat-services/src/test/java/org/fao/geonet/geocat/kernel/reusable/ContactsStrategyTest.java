package org.fao.geonet.geocat.kernel.reusable;

import com.google.common.collect.Sets;
import jeeves.server.UserSession;
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

import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_FIELD;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_EXTRA_NON_VALIDATED;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.LUCENE_UUID_FIELD;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces.CHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContactsStrategyTest extends AbstractSharedObjectStrategyTest {

    @Autowired
    ReusableObjManager manager;

    @Test
    public void testFind_NoXLink() throws Exception {
        final Metadata contact = addUserSubtemplate("testFind_NoXLink", true);

        final Element contactXml = contact.getXmlData(false);
        Element md = new Element("CHE_MD_Metadata", CHE).addContent(
                new Element("contact", GMD).addContent(contactXml)
        );

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, null, context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedContactEl = updated.getChild("contact", GMD);
        final String href = updatedContactEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("CI_RoleCode"));
        assertTrue(href, href.contains("subtemplate?"));
        assertTrue(href, href.contains("process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact"));
        assertTrue(href, href.contains("uuid=" + contact.getUuid()));

        LocalServiceRequest request = LocalServiceRequest.create(href);

        final Element paramXml = request.getParams();
        final Get get = new Get();

        final Element subtemplateXml = get.exec(paramXml, context);
        assertEqualsText("pointOfContact", subtemplateXml, "gmd:role/gmd:CI_RoleCode/@codeListValue", GMD);
        assertEqualsText("testFind_NoXLinkfirstname", subtemplateXml, "che:individualFirstName/gco:CharacterString", GMD, CHE, GCO);
        assertEqualsText("testFind_NoXLinklastname", subtemplateXml, "che:individualLastName/gco:CharacterString", GMD, CHE, GCO);
    }

    @Test
    public void testAddParentAndMain() throws Exception {
        Element sharedObjTmp = Xml.loadFile(AbstractSharedObjectTest.class.getResource(SHARED_USER_XML));
        Element md = new Element("CHE_MD_Metadata", CHE).addContent(
                new Element("contact", GMD).addContent(sharedObjTmp)
        );

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, null, context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedContactEl = updated.getChild("contact", GMD);
        final String href = updatedContactEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("CI_RoleCode"));
        assertTrue(href, href.contains("subtemplate?"));
        assertTrue(href, href.contains("process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact"));
        assertTrue(href, href.contains("uuid="));

        final String uuid = Utils.id(href);

        final MetadataRepository repository = _applicationContext.getBean(MetadataRepository.class);
        final Metadata oneByUuid = repository.findOneByUuid(uuid);

        assertNotNull(oneByUuid);
        assertEquals(MetadataType.SUB_TEMPLATE, oneByUuid.getDataInfo().getType());
        assertEquals("che:CHE_CI_ResponsibleParty", oneByUuid.getDataInfo().getRoot());
        assertEquals(LUCENE_EXTRA_NON_VALIDATED, oneByUuid.getDataInfo().getExtra());
        assertEquals(ISO19139cheSchemaPlugin.IDENTIFIER, oneByUuid.getDataInfo().getSchemaId());
        final Element xmlData = oneByUuid.getXmlData(false);

        final Element parentResponsibleParty = xmlData.getChild("parentResponsibleParty", CHE);
        assertNotNull(parentResponsibleParty);
        assertTrue(parentResponsibleParty.getChildren().isEmpty());
        final String parentHref = parentResponsibleParty.getAttributeValue("href", XLink.NAMESPACE_XLINK);
        assertNotNull(parentHref);
        assertTrue(parentHref.contains("custodian"));

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

            assertEquals(2, uuids.size());
        } finally {
            searchManager.releaseIndexReader(reader);
        }
    }

    @Test
    public void testAddMainUpdateParent() throws Exception {
        Element sharedObjTmp = Xml.loadFile(AbstractSharedObjectTest.class.getResource(SHARED_USER_XML));
        Element md = new Element("CHE_MD_Metadata", CHE).addContent(
                new Element("contact", GMD).addContent(sharedObjTmp)
        );

        String parentUUID = saveParentSubtemplate(sharedObjTmp, false);

        Xml.selectElement(md, "gmd:contact/*/che:parentResponsibleParty/che:CHE_CI_ResponsibleParty/gmd:role/*", Arrays.asList(GMD,
                CHE)).setAttribute("codeListValue", "author");
        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, "eng", context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedContactEl = updated.getChild("contact", GMD);
        final String href = updatedContactEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("CI_RoleCode"));
        assertTrue(href, href.contains("subtemplate?"));
        assertTrue(href, href.contains("process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact"));
        assertTrue(href, href.contains("uuid="));

        final String uuid = Utils.id(href);

        final MetadataRepository repository = _applicationContext.getBean(MetadataRepository.class);
        final Metadata addedMd = repository.findOneByUuid(uuid);

        assertNotNull(addedMd);
        assertEquals(MetadataType.SUB_TEMPLATE, addedMd.getDataInfo().getType());
        assertEquals("che:CHE_CI_ResponsibleParty", addedMd.getDataInfo().getRoot());
        assertEquals(LUCENE_EXTRA_NON_VALIDATED, addedMd.getDataInfo().getExtra());
        assertEquals(ISO19139cheSchemaPlugin.IDENTIFIER, addedMd.getDataInfo().getSchemaId());

        final Metadata parentMd = repository.findOneByUuid(parentUUID);
        assertEqualsText("pf name", parentMd.getXmlData(false), "che:individualFirstName/gco:CharacterString",
                CHE, GCO);
        assertEqualsText("author", parentMd.getXmlData(false), "gmd:role/*/@codeListValue",
                GMD);

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

            assertEquals(2, uuids.size());
        } finally {
            searchManager.releaseIndexReader(reader);
        }
    }

    @Test
    public void testAddMainValidatedParent() throws Exception {
        Element sharedObjTmp = Xml.loadFile(AbstractSharedObjectTest.class.getResource(SHARED_USER_XML));
        Element md = new Element("CHE_MD_Metadata", CHE).addContent(
                new Element("contact", GMD).addContent(sharedObjTmp)
        );

        String parentUUID = saveParentSubtemplate(sharedObjTmp, true);

        final ServiceContext context = createServiceContext();
        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, "eng", context);
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedContactEl = updated.getChild("contact", GMD);
        final String href = updatedContactEl.getAttributeValue("href", XLink.NAMESPACE_XLINK, null);
        assertTrue(href != null);
        assertTrue(href, href.contains("CI_RoleCode"));
        assertTrue(href, href.contains("subtemplate?"));
        assertTrue(href, href.contains("process=*//gmd:CI_RoleCode/@codeListValue~pointOfContact"));
        assertTrue(href, href.contains("uuid="));

        final String uuid = Utils.id(href);

        final MetadataRepository repository = _applicationContext.getBean(MetadataRepository.class);
        final Metadata addedMd = repository.findOneByUuid(uuid);

        assertNotNull(addedMd);
        assertEquals(MetadataType.SUB_TEMPLATE, addedMd.getDataInfo().getType());
        assertEquals("che:CHE_CI_ResponsibleParty", addedMd.getDataInfo().getRoot());
        assertEquals(LUCENE_EXTRA_NON_VALIDATED, addedMd.getDataInfo().getExtra());
        assertEquals(ISO19139cheSchemaPlugin.IDENTIFIER, addedMd.getDataInfo().getSchemaId());

        final Metadata parentMd = repository.findOneByUuid(parentUUID);
        assertEqualsText("original parent first name", parentMd.getXmlData(false), "che:individualFirstName/gco:CharacterString",
                CHE, GCO);

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

    private String saveParentSubtemplate(Element sharedObjTmp, boolean validated) throws Exception {
        final Element parent = sharedObjTmp.getChild("parentResponsibleParty", CHE);
        Element parentCopy = (Element) parent.getChild("CHE_CI_ResponsibleParty", CHE).clone();
        Xml.selectElement(parentCopy, "che:individualFirstName/gco:CharacterString", Arrays.asList(CHE, GCO)).
                setText("original parent first name");
        final String parentUUID = "parentUUID";
        saveSubtemplate(parentUUID, validated, parentCopy);
        ContactsStrategy contactsStrategy = new ContactsStrategy(_applicationContext);
        parent.setAttribute("href", contactsStrategy.createXlinkHref(parentUUID, null, "custodian"), XLink.NAMESPACE_XLINK);
        if (!validated) {
            parent.setAttribute("role", ReusableObjManager.NON_VALID_ROLE, XLink.NAMESPACE_XLINK);
        }
        return parentUUID;
    }

    @Test
    public void testCreateAsNeeded() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final ContactsStrategy contactsStrategy = new ContactsStrategy(_applicationContext);
        String href = contactsStrategy.createXlinkHref("", userSession, "author");
        contactsStrategy.createAsNeeded(href, userSession);
    }


    protected Metadata createDefaultSubtemplate(String seedData, boolean validated) throws Exception {
        return addUserSubtemplate("contact" + seedData, validated);
    }

    protected String getIsValidatedSpecificData() {
        return "author";
    }

    protected SharedObjectStrategy createReplacementStrategy() {
        return new ContactsStrategy(_applicationContext);
    }

    protected Element createMetadata(Element formatXml) {
        final Element md = new Element("CHE_MD_Metadata", CHE).addContent( Arrays.asList(
                new Element("language", GMD).addContent(new Element("CharacterString", GCO).setText("ger")),
                new Element("contact", GMD).addContent(formatXml),
                addLanguage("DE", "ger"),
                addLanguage("EN", "eng"),
                addLanguage("IT", "ita"),
                addLanguage("FR", "fre")
                ));

        return md;
    }

    private Element addLanguage(String id, String langCode) {
        return new Element("locale", GMD).addContent(
                new Element("PT_Locale", GMD).setAttribute("id", id).addContent(
                        new Element("languageCode", GMD).addContent(
                                new Element("LanguageCode", GMD).
                                        setAttribute("codeList", "#LanguageCode").
                                        setAttribute("codeListValue", langCode)
                        )
                )
        );
    }
}