package org.fao.geonet.schema.iso19139che;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.SingletonIterator;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

/**
 * Created by francois on 18/05/17.
 */
public class ISO19139cheUtil {


    public static Object posListToGM03Coords(Object node, Object coords, Object dim) {

        String[] coordsString = coords.toString().split("\\s+");

        if (coordsString.length % 2 != 0) {
            return "Error following data is not correct:" + coords.toString();
        }

        int dimension;
        if (dim == null) {
            dimension = 2;
        } else {
            try {
                dimension = Integer.parseInt(dim.toString());
            } catch (NumberFormatException e) {
                dimension = 2;
            }
        }
        StringBuilder results = new StringBuilder("<POLYLINE  xmlns=\"http://www.interlis.ch/INTERLIS2.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");


        for (int i = 0; i < coordsString.length; i++) {
            if (i % dimension == 0) {
                results.append("<COORD><C1>");
                results.append(coordsString[i]);
                results.append("</C1>");
            } else if (i > 0) {
                results.append("<C2>");
                results.append(coordsString[i]);
                results.append("</C2></COORD>");
            }
        }

        results.append("</POLYLINE>");
        try {
            Source source = new StreamSource(new ByteArrayInputStream(results.toString().getBytes("UTF-8")));
            DocumentInfo d = ((NodeInfo) node).getConfiguration().buildDocument(source);
            return SingletonIterator.makeIterator(d.iterateAxis(Axis.CHILD).next());
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
