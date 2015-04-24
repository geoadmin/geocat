package org.fao.geonet.kernel;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

/**
 * @author Jesse on 4/24/2015.
 */
public class GeocatDataManagerTest extends AbstractCoreIntegrationTest {
    @Autowired
    private DataManager dataManager;
    @Autowired
    private SchemaManager manager;

    @Test
    public void testAddAllThesaurusBlock() throws Exception {
        Element noKeywords = Xml.loadStream(GeocatDataManagerTest.class.getResourceAsStream("noKeywordsCheMd.xml"));
        dataManager.addAllThesaurusBlock(noKeywords, "iso19139.che");
        assertEqualsText("All Keywords", noKeywords, "gmd:identificationInfo//gmd:descriptiveKeywords//gmd:thesaurusName//gmd:title//gmd:LocalisedCharacterString[@locale = '#EN']", GMD, GCO);

        Element hasKeywords = Xml.loadStream(GeocatDataManagerTest.class.getResourceAsStream("hasKeywordsCheMd.xml"));
        dataManager.addAllThesaurusBlock(hasKeywords, "iso19139.che");
        assertEqualsText("All Keywords", hasKeywords, "gmd:identificationInfo//gmd:descriptiveKeywords//gmd:thesaurusName//gmd:title//gmd:LocalisedCharacterString[@locale = '#EN' and text() = 'All Keywords']", GMD, GCO);
        assertEqualsText("GEMET themes", hasKeywords, "gmd:identificationInfo//gmd:descriptiveKeywords//gmd:thesaurusName//gmd:title//gmd:LocalisedCharacterString[@locale = '#EN' and text() = 'GEMET themes']", GMD, GCO);

    }
}