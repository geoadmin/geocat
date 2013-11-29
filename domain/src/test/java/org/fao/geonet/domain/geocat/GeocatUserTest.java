package org.fao.geonet.domain.geocat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.UserRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.*;

/**
 * Geocat specific User tests
 * User: Jesse
 * Date: 11/29/13
 * Time: 8:36 AM
 */
public class GeocatUserTest {
    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        User user = newUser(_inc);

        final GeocatUserInfo geocatUserInfo = user.getGeocatUserInfo();
        Phone phone1 = user.getPhones().get(0);


        final Element element = user.asXml();

        Element phonesXml = element.getChild("phones");
        assertNotNull(phonesXml);
        assertEquals(1, phonesXml.getChildren().size());

        Element phoneXml = phonesXml.getChild("phon");
        assertNotNull(phoneXml);

        assertEquals(phone1.getDirectnumber(), phoneXml.getChildText("directnumber"));
        assertEquals(phone1.getFacsimile(), phoneXml.getChildText("facsimile"));
        assertEquals(phone1.getMobile(), phoneXml.getChildText("mobile"));
        assertEquals(phone1.getPhone(), phoneXml.getChildText("phone"));

        final Element geocatUserInfoXml = element.getChild("geocatuserinfo");
        assertNotNull(geocatUserInfoXml);

        assertEquals(geocatUserInfo.getContactinstructions(), geocatUserInfoXml.getChildText("contactinstructions"));
        assertEquals(geocatUserInfo.getHoursofservice(), geocatUserInfoXml.getChildText("hoursofservice"));
        assertEquals(geocatUserInfo.getOnlinedescription(), geocatUserInfoXml.getChildText("onlinedescription"));
        assertEquals(geocatUserInfo.getOnlinename(), geocatUserInfoXml.getChildText("onlinename"));
        assertEquals(geocatUserInfo.getOnlineresource(), geocatUserInfoXml.getChildText("onlineresource"));
        assertEquals(geocatUserInfo.getOrgacronym(), geocatUserInfoXml.getChildText("orgacronym"));
        assertEquals(geocatUserInfo.getPositionname(), geocatUserInfoXml.getChildText("positionname"));
        assertEquals(geocatUserInfo.getPublicaccess(), geocatUserInfoXml.getChildText("publicaccess"));
        assertEquals(geocatUserInfo.getParentInfo().toString(), geocatUserInfoXml.getChildText("parentinfo"));
        assertEquals(""+geocatUserInfo.isValidated(), geocatUserInfoXml.getChildText("validated"));
    }

    public static User newUser(AtomicInteger _inc) {
        final User user = UserRepositoryTest.newUser(_inc);
        user.setId(43234);
        final GeocatUserInfo geocatUserInfo = user.getGeocatUserInfo();
        geocatUserInfo.setContactinstructions("contInst");
        geocatUserInfo.setHoursofservice("hoursService");
        geocatUserInfo.setOnlinedescription("onlineDesc");
        geocatUserInfo.setOnlinename("<DE>onlineNameDE</DE><EN>onlineNameEN</EN>");
        geocatUserInfo.setOnlineresource("onlineRes");
        geocatUserInfo.setOrgacronym("orgAcro");
        geocatUserInfo.setParentInfo(113435234);
        geocatUserInfo.setPositionname("posName");
        geocatUserInfo.setPublicaccess("pubacc");
        geocatUserInfo.setValidated(false);

        Phone phone1 = new Phone();
        phone1.setDirectnumber("dirNum");
        phone1.setFacsimile("facNum");
        phone1.setMobile("mobNum");
        phone1.setPhone("phoneNum");
        user.getPhones().add(phone1);

        return user;
    }
}
