package org.fao.geonet.geocat.kernel;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AapMetadataReportTest {

    private final AapMetadataReport amr = new AapMetadataReport();

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {}
    
    @Test
    public void extractAapInfoTest() throws Exception {
        URL rawMdUrl = this.getClass().getResource("mdaaptest.xml");
        assumeTrue(rawMdUrl != null);
        File rawMdF = new File(rawMdUrl.toURI());
        assumeTrue(rawMdF.exists());

        String rawMd = FileUtils.readFileToString(rawMdF);
        Metadata tested = new Metadata();
        tested.setData(rawMd);

        String el = Xml.getString(amr.extractAapInfo(tested));

        assertTrue("Unexpected title",
                   el.contains("<title>Lisières forestières prioritaires</title>"));
        assertTrue("Unexpected geodata ID",
                el.contains("<identifier>101.2-TG</identifier>"));
        assertTrue("Unexpected UUID",
                el.contains("<uuid>d2ab7e4e-d135-4442-b0af-2f8892d87843</uuid>"));
        assertTrue("Unexpected geodatatype",
                el.contains("<geodatatype>Ja</geodatatype>"));
        assertTrue("Unexpected owner",
                el.contains("<owner>SFF</owner>"));
        assertTrue("Unexpected owner",
        el.contains("<specialistAuthority>Camptocamp</specialistAuthority>"));
        assertTrue("Unexpected update frequency",
                   el.contains("<updateFrequency>userDefined</updateFrequency>"));
        assertTrue("Unexpected duration of conservation",
                   el.contains("<durationOfConservation>3</durationOfConservation>"));
        assertTrue("Unexpected comment on duration",
                   el.contains("<commentOnDuration>Sample comment on the duration of conservation</commentOnDuration>"));
        assertTrue("Unexpected comment on archival",
                   el.contains("<commentOnArchival>Sample comment on "
                           + "the archival value</commentOnArchival>"));
        assertTrue("Unexpected appraisal of archival value",
                   el.contains("<appraisalOfArchival>S</appraisalOfArchival>"));
        assertTrue("Unexpected reason for archiving value",
                   el.contains("<reasonForArchiving>definingPowers</reasonForArchiving>")
                );
    }

    @Test
    public void extractAapInfoOnNonAapMdTest() throws Exception {
        URL rawMdUrl = this.getClass().getResource("mdaaptest-noaap.xml");
        assumeTrue(rawMdUrl != null);
        File rawMdF = new File(rawMdUrl.toURI());
        assumeTrue(rawMdF.exists());

        String rawMd = FileUtils.readFileToString(rawMdF);
        Metadata tested = new Metadata();
        tested.setData(rawMd);

        String el = Xml.getString(amr.extractAapInfo(tested));

        assertTrue("Unexpected content element extracted from mdaaptest-noaap.xml",
                el.contains("<durationOfConservation />") &&
                el.contains("<commentOnDuration />")      &&
                el.contains("<commentOnArchival />")      &&
                el.contains("<reasonForArchiving />")     &&
                el.contains("<appraisalOfArchival />"));
    }
    
    @Test
    public void extractAapInfoFromCustomerProvidedMd() throws Exception {
        URL rawMdUrl = this.getClass().getResource("aap.xml");
        assumeTrue(rawMdUrl != null);
        File rawMdF = new File(rawMdUrl.toURI());
        assumeTrue(rawMdF.exists());
        

        String rawMd = FileUtils.readFileToString(rawMdF);
        Metadata tested = new Metadata();
        tested.setData(rawMd);

        
        String el = Xml.getString(amr.extractAapInfo(tested));  
        System.out.println(el);
    }
    
    @Test
    public void testXpathIndexFieldAap() throws Exception {
        // mdaaptest-noaap.xml for not containing kw, aap.xml for a containing one
        URL rawMdUrl = this.getClass().getResource("aap.xml");
        assumeTrue(rawMdUrl != null);
        File rawMdF = new File(rawMdUrl.toURI());
        assumeTrue(rawMdF.exists());
        String rawMd = FileUtils.readFileToString(rawMdF);

        // xpath used for AAP field indexation (see iso19139.che/default.xsl around line 453)
        String xpath = "gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:descriptiveKeywords"+
                "/gmd:MD_Keywords/gmd:keyword/gco:CharacterString[text() = 'AAP-Bund']";

        boolean b = Xml.selectBoolean(Xml.loadString(rawMd, false), xpath);
        assertTrue("Expected true, false found", b);
    }
    
}
