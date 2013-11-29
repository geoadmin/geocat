package org.fao.geonet.services.user;

import static org.junit.Assert.*;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.geocat.GeocatUserTest;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: Jesse
 * Date: 11/29/13
 * Time: 9:33 AM
 */
public class GeocatGetTest extends AbstractServiceIntegrationTest {
    @Autowired
    private UserRepository repo;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testExec() throws Exception {
        final User user = repo.save(GeocatUserTest.newUser(_inc).setProfile(Profile.Shared));

        final ServiceContext serviceContext = createServiceContext();
        final Element params = createParams(Pair.read("id", "" + user.getId()));

        final Get get = new Get();
        final Element result = get.exec(params, serviceContext);

        final Element element = new Element("root").addContent(result);
        System.out.println(Xml.getString(element));
        final Element transform = Xml.transform(element, getStyleSheets() + "/../shared-user/user-xml.xsl");

        assertEqualsText("onlineNameDE", transform, "*//gmd:CI_OnlineResource/gmd:name//gmd:LocalisedCharacterString[@locale = '#DE']",
                Geonet.Namespaces.GMD);
        assertEqualsText("onlineNameEN", transform, "*//gmd:CI_OnlineResource/gmd:name//gmd:LocalisedCharacterString[@locale = '#EN']",
                Geonet.Namespaces.GMD);
    }
}
