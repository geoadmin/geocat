package org.fao.geonet.services;

import jeeves.constants.Jeeves;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Adds extra bean required for services tests.
 *
 * User: Jesse
 * Date: 10/17/13
 * Time: 9:53 AM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:services-repository-test-context.xml")
public abstract class AbstractServiceIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private GeonetworkDataDirectory _dataDir;

    /**
     * Create a root element, add the gui element with the strings of the language, add serviceResponse to the root,
     * the transform the element with the given xslt.
     *
     * @param serviceResponse
     * @param xsltWebappBase
     * @param language
     * @return
     * @throws Exception
     */
    public Element transformServiceResult(Element serviceResponse, String xsltWebappBase, String language) throws Exception {
        Element withExtraData = new Element(Jeeves.Elem.ROOT);
        Element gui = new Element(Jeeves.Elem.GUI);
        gui.addContent(Xml.loadFile(_dataDir.getWebappDir().resolve("loc/" + language + "/xml/strings.xml")));

        withExtraData.addContent(gui);
        withExtraData.addContent(serviceResponse);

        return Xml.transform(withExtraData, _dataDir.getWebappDir().resolve(xsltWebappBase));
    }
}
