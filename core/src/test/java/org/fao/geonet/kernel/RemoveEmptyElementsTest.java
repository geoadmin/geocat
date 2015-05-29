package org.fao.geonet.kernel;

import com.google.common.collect.Lists;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Jesse on 5/29/2015.
 */
public class RemoveEmptyElementsTest {

    private static final List<Namespace> NS = Lists.newArrayList(ISO19139Namespaces.GMD, ISO19139Namespaces.GCO,
            ISO19139cheNamespaces.CHE);

    @Test
    public void testApplyContactXml() throws Exception {
        Element contactXml = Xml.loadString("<che:CHE_CI_ResponsibleParty xmlns:che=\"http://www.geocat.ch/2008/che\"\n"
                                            + "                              xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n"
                                            + "                              xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"\n"
                                            + "                              xmlns:xsi=\"http://www.w3"
                                            + ".org/2001/XMLSchema-instance\"\n"
                                            + "                              xmlns:geonet=\"http://www.fao.org/geonetwork\"\n"
                                            + "                              gco:isoType=\"gmd:CI_ResponsibleParty\">\n"
                                            + "    <gmd:organisationName xsi:type=\"gmd:PT_FreeText_PropertyType\">\n"
                                            + "       <gmd:PT_FreeText>\n"
                                            + "          <gmd:textGroup>\n"
                                            + "             <gmd:LocalisedCharacterString locale=\"#DE\">~~ Template "
                                            + "Organization Name "
                                            + "~~</gmd:LocalisedCharacterString>\n"
                                            + "          </gmd:textGroup>\n"
                                            + "          <gmd:textGroup>\n"
                                            + "             <gmd:LocalisedCharacterString locale=\"#FR\">~~ Template "
                                            + "Organization Name "
                                            + "~~</gmd:LocalisedCharacterString>\n"
                                            + "          </gmd:textGroup>\n"
                                            + "          <gmd:textGroup>\n"
                                            + "             <gmd:LocalisedCharacterString locale=\"#IT\">~~ Template "
                                            + "Organization Name "
                                            + "~~</gmd:LocalisedCharacterString>\n"
                                            + "          </gmd:textGroup>\n"
                                            + "          <gmd:textGroup>\n"
                                            + "             <gmd:LocalisedCharacterString locale=\"#EN\">~~ Template "
                                            + "Organization Name "
                                            + "~~</gmd:LocalisedCharacterString>\n"
                                            + "          </gmd:textGroup>\n"
                                            + "       </gmd:PT_FreeText>\n"
                                            + "    </gmd:organisationName>\n"
                                            + "    <gmd:contactInfo>\n"
                                            + "       <gmd:CI_Contact>\n"
                                            + "          <gmd:phone>\n"
                                            + "             <che:CHE_CI_Telephone gco:isoType=\"gmd:CI_Telephone\">\n"
                                            + "                <gmd:voice>\n"
                                            + "                   <gco:CharacterString/>\n"
                                            + "                </gmd:voice>\n"
                                            + "             </che:CHE_CI_Telephone>\n"
                                            + "          </gmd:phone>\n"
                                            + "          <gmd:address>\n"
                                            + "             <che:CHE_CI_Address gco:isoType=\"gmd:CI_Address\">\n"
                                            + "                <gmd:city>\n"
                                            + "                   <gco:CharacterString/>\n"
                                            + "                </gmd:city>\n"
                                            + "                <gmd:postalCode>\n"
                                            + "                  <gco:CharacterString/>\n"
                                            + "                </gmd:postalCode>\n"
                                            + "                <gmd:electronicMailAddress>\n"
                                            + "                   <gco:CharacterString>~~ Template Email "
                                            + "~~</gco:CharacterString>\n"
                                            + "                </gmd:electronicMailAddress>\n"
                                            + "                <che:streetName>\n"
                                            + "                   <gco:CharacterString/>\n"
                                            + "                </che:streetName>\n"
                                            + "                <che:streetNumber>\n"
                                            + "                   <gco:CharacterString/>\n"
                                            + "                </che:streetNumber>\n"
                                            + "             </che:CHE_CI_Address>\n"
                                            + "          </gmd:address>\n"
                                            + "       </gmd:CI_Contact>\n"
                                            + "   </gmd:contactInfo>\n"
                                            + "    <gmd:role>\n"
                                            + "       <gmd:CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList"
                                            + ".xml#CI_RoleCode\"\n"
                                            + "                        codeListValue=\"pointOfContact\"/>\n"
                                            + "   </gmd:role>\n"
                                            + "    <che:individualFirstName>\n"
                                            + "       <gco:CharacterString>~~ Template First Name ~~</gco:CharacterString>\n"
                                            + "   </che:individualFirstName>\n"
                                            + "    <che:individualLastName>\n"
                                            + "       <gco:CharacterString>~~ Template Last Name ~~</gco:CharacterString>\n"
                                            + "   </che:individualLastName>\n"
                                            + "</che:CHE_CI_ResponsibleParty>", false);

        RemoveEmptyElements.apply(contactXml);
        
        assertNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "*//che:streetName", NS));
        assertNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "*//che:streetNumber", NS));
        assertNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "*//gmd:phone", NS));
        assertNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "*//gmd:city", NS));
        assertNotNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "*//gmd:electronicMailAddress", NS));
        assertNotNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "che:individualFirstName", NS));
        assertNotNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "che:individualLastName", NS));
        assertNotNull(Xml.getString(contactXml), Xml.selectElement(contactXml, "gmd:role", NS));
    }
}