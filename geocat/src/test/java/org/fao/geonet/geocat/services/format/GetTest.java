package org.fao.geonet.geocat.services.format;

import static org.junit.Assert.*;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.geocat.Format;
import org.fao.geonet.repository.geocat.FormatRepository;
import org.fao.geonet.repository.geocat.specification.FormatSpecsTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: Jesse
 * Date: 11/29/13
 * Time: 1:58 PM
 */
public class GetTest extends AbstractServiceIntegrationTest {

    @Autowired
    FormatRepository _repo;

    @Test
    public void testGetFormat() throws Exception {
        final Format format = FormatSpecsTest.createFormat(1, false);
        final Format save = _repo.save(format);

        final ServiceContext serviceContext = createServiceContext();
        final Element params = createParams(Pair.read("id", "" + save.getId()));

        final Element response = new Get().exec(params, serviceContext);

        assertEquals(Jeeves.Elem.RESPONSE, response.getName());
        assertNotNull(response.getChild(Jeeves.Elem.RECORD));

        Element afterTransform = transformServiceResult(response, "xsl/format-xml.xsl", "eng");

        assertEquals("MD_Format", afterTransform.getName());

        Element name = afterTransform.getChild("name", Geonet.Namespaces.GMD);
        assertNotNull(name);

        Element version = afterTransform.getChild("version", Geonet.Namespaces.GMD);
        assertNotNull(version);
    }
}
