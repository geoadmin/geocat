package org.fao.geonet.geocat.services.reusable;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.fao.geonet.geocat.kernel.reusable.ReplacementStrategy;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListSharedContactsTest extends AbstractSharedObjectTest {

    @Autowired
    private SearchManager searchManager;

    @Test
    public void testIndexedContact() throws Exception {
        addUserSubtemplate("listContacts", true);

        final IndexAndTaxonomy index = searchManager.getNewIndexReader("eng");
        try {
            Set<String> locales = Sets.newHashSet();
            for (int i = 0; i < index.indexReader.numDocs(); i++) {
                final Document document = index.indexReader.document(i);
                String locale = document.get("_locale");
                String prefix = locale.substring(0, 2).toLowerCase();

                if (prefix.equals("ge")) {
                    prefix = "de";
                }
                locales.add(prefix);
                assertField(document, "organisationName", prefix + " listContactsorg");
                assertField(document, "positionName", prefix + " listContactsposname");
                assertField(document, "voice", "1233454565", "0988765r", "76904367943");
                assertField(document, "facsimile", "123234235", "0988765r", "76904367943");
                assertField(document, "directNumber", "76904367943");
                assertField(document, "mobile", "76904367943");
                assertField(document, "city", "listContactscity1");
                assertField(document, "administrativeArea", "listContactsstate1");
                assertField(document, "postalCode", "zip1");
                assertField(document, "country", "CA");
                assertField(document, "electronicMailAddress", "listContactsemail1@gmail.com", "listContactsemail2@gmail.com",
                        "listContactsemail3@gmail.com");
                assertField(document, "streetName", "listContactsstreet name");
                assertField(document, "streetNumber", "123");
                assertField(document, "addressLine", "listContactsaddress");
                assertField(document, "postBox", "12345");
                assertField(document, "linkage", prefix + " listContactsurl");
                assertField(document, "protocol", "text/html");
                assertField(document, "name", "listContactsname");
                assertField(document, "description", prefix + " listContactsdesc");
                assertField(document, "hoursOfService", "listContactshour of service");
                assertField(document, "contactInstructions", prefix + " listContactscontact_inst");
                assertField(document, "role", "pointOfContact");
                assertField(document, "individualFirstName", "listContactsfirstname");
                assertField(document, "individualLastName", "listContactslastname");
                assertField(document, "organisationAcronym", prefix + " listContactsorgac");
                assertField(document, "_extra", "validated");
                assertField(document, "_root", "che:CHE_CI_ResponsibleParty");
            }
            assertEquals(4, locales.size());
            assertTrue(locales.contains("en"));
            assertTrue(locales.contains("de"));
            assertTrue(locales.contains("fr"));
            assertTrue(locales.contains("it"));
        } finally {
            searchManager.releaseIndexReader(index);
        }
    }

    @Test
    public void testExec() throws Exception {
        addFormatSubtemplate("valid", true);
        addUserSubtemplate("valid1", true);
        addUserSubtemplate("valid2", true, "EN");
        addUserSubtemplate("invalid", false);
        final List listSharedObjects = new List();

        ServiceContext context = createServiceContext();
        Element response = listSharedObjects.exec(
                createParams(read("type", ReusableObjManager.CONTACTS), read("validated", true)),
                context);

        assertEquals(2, response.getChildren().size());
        validList(response, "valid1", 0);
        validList(response, "valid2", 1);

        response = listSharedObjects.exec(
                createParams(read("type", "contacts"), read("nonvalidated", true)),
                context);

        assertEquals(1, response.getChildren().size());
        validList(response, "invalid", 0);
    }

    private void validList(Element response, String uuid, int i) {
        final Element el1 = (Element) response.getChildren().get(i);
        assertEquals(uuid, el1.getChildText(ReplacementStrategy.REPORT_ID));
        assertEquals(uuid + "firstname " + uuid + "lastname (" + uuid + "email1@gmail.com)",
                el1.getChildText(ReplacementStrategy.REPORT_DESC));
        assertEquals("local://subtemplate?uuid=" + uuid, el1.getChildText(ReplacementStrategy.REPORT_XLINK));
        final String search1 = el1.getChildText(ReplacementStrategy.REPORT_SEARCH);
        assertTrue(search1, search1.contains(uuid) && search1.contains(uuid + "firstname") && search1.contains(uuid + "lastname"));
        assertTrue(el1.getChild(ReplacementStrategy.REPORT_URL) != null);
    }
}