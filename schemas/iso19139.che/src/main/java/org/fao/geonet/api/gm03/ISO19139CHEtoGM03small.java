package org.fao.geonet.api.gm03;

import org.apache.commons.lang.ArrayUtils;
import org.fao.geonet.utils.IO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;

public class ISO19139CHEtoGM03small extends ISO19139CHEtoGM03Base {
    private static final String GML = "http://www.geocat.ch/2003/05/gateway/GML";

    public ISO19139CHEtoGM03small(Path schemaLocation, Path xslFilename) throws SAXException, TransformerConfigurationException {
        super(schemaLocation, xslFilename);
    }

    protected boolean wantIntermediate() {
        return false;
    }

    protected void flatten(Document source) throws FlattenerException {
    }

    protected void removeDuplicates(Document doc) throws FlattenerException {
    }

    protected String getGmlCoordinateNs() {
        return null;
    }

    protected Element createCoordinate(Document doc, String[] vals) {
        Element coordNode = doc.createElementNS(GML, "GML:pos");
        StringBuffer coords = new StringBuffer();
        for (int k = 0; k < vals.length; k++) {
            String val = vals[k];
            if (k > 0) {
                coords.append(" ");
            }
            coords.append(val);
        }
        coordNode.setTextContent(coords.toString());
        return coordNode;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, FlattenerException,
            TransformerException {
        final String xslFilename = args[0];
        final String schemaFilename = args[1];
        final String[] xmlFilenames = (String[]) ArrayUtils.subarray(args, 2, args.length);

        Path schemaLocation = null;
        if (!schemaFilename.equalsIgnoreCase("no")) {
            schemaLocation = IO.toPath(schemaFilename);
        }
        ISO19139CHEtoGM03small converter = new ISO19139CHEtoGM03small(schemaLocation, IO.toPath(xslFilename));

        converter.convert(xmlFilenames, "ISO19139CHEtoGM03small.main");
    }
}