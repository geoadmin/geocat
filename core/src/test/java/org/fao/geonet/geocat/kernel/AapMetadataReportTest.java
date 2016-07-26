package org.fao.geonet.geocat.kernel;

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

        Element el = amr.extractAapInfo(tested);
        System.out.println(Xml.getString(el));
    }
    
}
