package org.fao.geonet.geocat.services.reusable;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.fao.geonet.geocat.kernel.reusable.ReusableObjManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.fao.geonet.domain.Pair.read;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.REPORT_DESC;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.REPORT_ID;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.REPORT_SEARCH;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.REPORT_URL;
import static org.fao.geonet.geocat.kernel.reusable.SharedObjectStrategy.REPORT_XLINK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListSharedFormatsTest extends AbstractSharedObjectTest {

    @Autowired
    private SearchManager searchManager;

    @Test
    public void testIndexedFormat() throws Exception {
        addFormatSubtemplate("listFormat", true);

        final IndexAndTaxonomy index = searchManager.getNewIndexReader("eng");
        try {
            Set<String> locales = Sets.newHashSet();
            for (int i = 0; i < index.indexReader.numDocs(); i++) {
                final Document document = index.indexReader.document(i);
                String locale = document.get("_locale");
                String prefix = locale.substring(0, 2).toLowerCase();

                locales.add(prefix);
                assertField(document, "name", "listFormatname");
                assertField(document, "version", "listFormatversion");
                assertField(document, "_extra", "validated");
                assertField(document, "_root", "gmd:MD_Format");
            }
            assertEquals(1, locales.size());
            assertTrue(locales.contains("en"));
        } finally {
            searchManager.releaseIndexReader(index);
        }
    }

    @Test
    public void testExec() throws Exception {
        addUserSubtemplate("validFormat", true);
        addFormatSubtemplate("valid1", true);
        addFormatSubtemplate("valid2", true);
        addUserSubtemplate("invalid", false);
        final List listSharedObjects = new List();

        ServiceContext context = createServiceContext();
        final Element response = listSharedObjects.exec(
                createParams(read("type", ReusableObjManager.FORMATS), read("validated", true)),
                context);

        assertEquals(2, response.getChildren().size());
        validList(response, "valid1", 0);
        validList(response, "valid2", 1);
    }

    private void validList(Element response, String uuid, int i) {
        final Element el1 = (Element) response.getChildren().get(i);
        assertEquals(uuid, el1.getChildText(REPORT_ID));
        assertEquals(uuid + "name (" + uuid + "version)", el1.getChildText(REPORT_DESC));
        assertEquals("local://subtemplate?uuid=" + uuid, el1.getChildText(REPORT_XLINK));
        final String search1 = el1.getChildText(REPORT_SEARCH);
        assertTrue(search1, search1.contains(uuid) && search1.contains(uuid + "name") && search1.contains(uuid + "version"));
        assertTrue(el1.getChild(REPORT_URL) != null);
    }
}