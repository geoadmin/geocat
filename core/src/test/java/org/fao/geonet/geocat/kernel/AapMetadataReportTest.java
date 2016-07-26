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
        assertTrue("Unexpected content element extracted from mdaaptest.xml",
                   el.contains("<title>Lisières forestières prioritaires</title>")              &&
                   el.contains("<uuid>d2ab7e4e-d135-4442-b0af-2f8892d87843</uuid>")             &&
                   el.contains("<updateFrequency>userDefined</updateFrequency>")                &&
                   el.contains("<durationOfConservation>3</durationOfConservation>")            &&
                   el.contains("<commentOnDuration>3 years should be sufficient "
                           + "for any MD - sample comment</commentOnDuration>")                 &&
                   el.contains("<commentOnArchival>Sample comment on "
                           + "the archival value</commentOnArchival>")                          &&
                   el.contains("<appraisalOfArchival>S</appraisalOfArchival>")                  &&
                   el.contains("<reasonForArchiving>evidenceOfBusinessPractice</reasonForArchiving>")
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
    
}
