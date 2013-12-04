package org.fao.geonet.geocat.services.format;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
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
 * Time: 11:28 AM
 */
public class ListTest extends AbstractServiceIntegrationTest {

    @Autowired
    FormatRepository _repo;

    @Test
    public void testExec() throws Exception {
        Format format = FormatSpecsTest.createFormat(1, false);
        format = _repo.save(format);

        final ServiceContext context = createServiceContext();
        Element params = createParams(Pair.read("name", ""));

        final Element response = new List().exec(params, context);
        assertEquals(Jeeves.Elem.RESPONSE, response.getName());
        assertEquals(1, response.getChildren("record").size());
        final Element record = response.getChild("record");
        assertEquals("" + format.getId(), record.getChildText("id"));
        assertEquals(format.getName(), record.getChildText("name"));
        assertEquals(format.getVersion(), record.getChildText("version"));

    }
}
