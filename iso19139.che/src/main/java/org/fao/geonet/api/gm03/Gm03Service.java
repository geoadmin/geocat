package org.fao.geonet.api.gm03;

import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.nio.file.Path;

public class Gm03Service extends Gm03BaseService {
    protected ISO19139CHEtoGM03Base createConverter(Path xsdFile) throws SAXException, TransformerConfigurationException {
        return new ISO19139CHEtoGM03(xsdFile, xsl.toAbsolutePath());
    }

}
