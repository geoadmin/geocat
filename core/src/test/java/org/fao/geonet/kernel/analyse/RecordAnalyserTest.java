package org.fao.geonet.kernel.analyse;


import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class RecordAnalyserTest {

    @Test
    public void processMetadata() throws IOException, JDOMException {
        Element firstMd = getMdAsXml("../valid-metadata.iso19139.xml");
        RecordAnalyser toTest = new RecordAnalyser();

        toTest.processMetadata(firstMd, "uuid");



    }

    @Test
    public void persist() throws Exception {
        Element firstMd = getMdAsXml("../valid-metadata.iso19139.xml");
        Element secondMd = getMdAsXml("../vicinityMap.xml");
        RecordAnalyser toTest = new RecordAnalyser();

        toTest.processMetadata(firstMd, "first");
        toTest.processMetadata(secondMd, "second");
        //toTest.persist("/home/cmangeat/test.csv");
        toTest.persist("/home/cmangeat/test.arff");
    }


    private Element getMdAsXml(String ressourcePath) throws IOException, JDOMException {
        URL mdResourceUrl = RecordAnalyserTest.class.getResource(ressourcePath);
        return Xml.loadStream(mdResourceUrl.openStream());
    }
}
