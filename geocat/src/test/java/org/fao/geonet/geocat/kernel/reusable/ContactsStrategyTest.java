package org.fao.geonet.geocat.kernel.reusable;

import jeeves.xlink.XLink;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.geocat.kernel.reusable.log.ReusableObjectLogger;
import org.fao.geonet.geocat.services.reusable.AbstractSharedObjectTest;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContactsStrategyTest extends AbstractSharedObjectTest {

    @Autowired
    ReusableObjManager manager;

    @Test
    public void testFind_NoXLink() throws Exception {
        final Metadata contact = addUserSubtemplate("testFind_NoXLink", true);

        final Element contactXml = contact.getXmlData(false);
        Element md = new Element("CHE_MD_Metadata", ISO19139cheNamespaces.CHE).addContent(
                new Element("contact", ISO19139Namespaces.GMD).addContent(contactXml)
        );

        ProcessParams params = new ProcessParams(ReusableObjectLogger.THREAD_SAFE_LOGGER, null, md,
                md, false, null, createServiceContext());
        final List<Element> process = manager.process(params);

        assertEquals(1, process.size());
        Element updated = process.get(0);
        final Element updatedContactEl = updated.getChild("contact", ISO19139Namespaces.GMD);
        final String href = updatedContactEl.getAttributeValue("href", XLink.NS_XLINK);
        assertTrue(href != null);
        assertTrue(href, href.contains("role"));
    }



    @Test
    public void testAdd() throws Exception {

    }

    @Test
    public void testPerformDelete() throws Exception {

    }

    @Test
    public void testMarkAsValidated() throws Exception {

    }

    @Test
    public void testUpdateObject() throws Exception {

    }

    @Test
    public void testIsValidated() throws Exception {

    }

    @Test
    public void testCreateAsNeeded() throws Exception {

    }
}