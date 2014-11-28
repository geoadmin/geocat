package org.fao.geonet.geocat.services.gm03;

import org.xml.sax.SAXException;

import java.nio.file.Path;
import javax.xml.transform.TransformerConfigurationException;

public class Gm03smallService extends Gm03BaseService {
    protected ISO19139CHEtoGM03Base createConverter(Path xsdFile) throws SAXException, TransformerConfigurationException {
        return new ISO19139CHEtoGM03small(xsdFile, xsl.toAbsolutePath());
    }
}