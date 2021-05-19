package org.fao.geonet.schema.iso19139che;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.sf.saxon.om.*;
import net.sf.saxon.type.Type;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.extent.ExtentHelper;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xsd.Encoder;
import org.geotools.xsd.Parser;
import org.jdom.Namespace;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by francois on 18/05/17.
 */
public class ISO19139cheUtil {

    interface GeomWriter {
        Object write(ExtentHelper.ExtentTypeCode code, MultiPolygon geometry) throws Exception;
    }

    private static final GMLConfiguration GML3_CONFIG = new org.geotools.gml3.GMLConfiguration();

    static {
        @SuppressWarnings("unchecked")
        Set<QName> props = ISO19139cheUtil.GML3_CONFIG.getProperties();
        QName toAdd = org.geotools.gml3.GMLConfiguration.NO_SRS_DIMENSION;
        props.add(toAdd);
    }

    public static final org.geotools.gml2.GMLConfiguration GML2_CONFIG = new org.geotools.gml2.GMLConfiguration();
    private static final Random RANDOM = new Random();
    public static final Namespace CHE_NAMESPACE = ISO19139cheNamespaces.CHE;

    static Parser gml3Parser() {
        return new Parser(GML3_CONFIG);
    }

    static Parser gml2Parser() {
        return new Parser(GML2_CONFIG);
    }

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

    public static String writeXml(Node doc) throws Exception {
        // Prepare the DOM document for writing
        Source source = new DOMSource(doc);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Prepare the output file
        Result result = new StreamResult(out);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactoryFactory.getTransformerFactory().newTransformer();
        xformer.transform(source, result);
        return out.toString("utf-8");
    }

    public static String writeXml(NodeInfo doc) throws Exception {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // Prepare the output file
            Result result = new StreamResult(out);
            // Write the DOM document to the file
            Transformer xformer = TransformerFactoryFactory.getTransformerFactory().newTransformer();

            xformer.transform(doc, result);
            return out.toString("utf-8").replaceFirst("<\\?xml.+?>", "").replace("xmlns=\"\" ", "");
        } catch (Throwable e) {
            return doc.getStringValue();
        }
    }

    public static Object bbox(Object description, Object src) throws Exception {

        final NodeInfo ni = (NodeInfo) src;
        return ISO19139cheUtil.combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new ISO19139cheUtil.GeomWriter() {

            public Object write(ExtentHelper.ExtentTypeCode code, MultiPolygon geometry) throws Exception {

                Envelope bbox = geometry.getEnvelopeInternal();

                String template = "<gmd:EX_GeographicBoundingBox xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">" +
                        "<gmd:extentTypeCode>" +
                        "<gco:Boolean>%s</gco:Boolean>" +
                        "</gmd:extentTypeCode>" +
                        "<gmd:westBoundLongitude>" +
                        "<gco:Decimal>%s</gco:Decimal>" +
                        "</gmd:westBoundLongitude>" +
                        "<gmd:eastBoundLongitude>" +
                        "<gco:Decimal>%s</gco:Decimal>" +
                        "</gmd:eastBoundLongitude>" +
                        "<gmd:southBoundLatitude>" +
                        "<gco:Decimal>%s</gco:Decimal>" +
                        "</gmd:southBoundLatitude>" +
                        "<gmd:northBoundLatitude>" +
                        "<gco:Decimal>%s</gco:Decimal>" +
                        "</gmd:northBoundLatitude>" +
                        "</gmd:EX_GeographicBoundingBox>";

                String extentTypeCode = code == ExtentHelper.ExtentTypeCode.EXCLUDE ? "false" : "true";
                String xml = String.format(template, extentTypeCode, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());

                Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                DocumentInfo doc = ni.getConfiguration().buildDocument(source);
                return SingletonIterator.makeIterator(doc);
            }
        });
    }

    public static Object multipolygon(Object description, Object src) throws Exception {

        final NodeInfo ni = ((NodeInfo) src);
        return ISO19139cheUtil.combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new ISO19139cheUtil.GeomWriter() {

            public Object write(ExtentHelper.ExtentTypeCode code, MultiPolygon geometry) throws Exception {
                geometry.setUserData(null);
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final Encoder encoder = new Encoder(GML3_CONFIG);
                encoder.setIndenting(false);
                encoder.setOmitXMLDeclaration(true);
                encoder.setEncoding(Charset.forName("UTF-8"));
                ExtentHelper.addGmlId(geometry);
                encoder.encode(geometry, org.geotools.gml3.GML.geometryMember, outputStream);

                StringBuilder builder = new StringBuilder("<gmd:EX_BoundingPolygon xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"><gmd:extentTypeCode><gco:Boolean>").
                        append(code == ExtentHelper.ExtentTypeCode.EXCLUDE ? "false" : "true").
                        append("</gco:Boolean></gmd:extentTypeCode><gmd:polygon>");

                Source xml1 = new StreamSource(new ByteArrayInputStream(outputStream.toByteArray()));
                DocumentInfo doc1 = ni.getConfiguration().buildDocument(xml1);
                AxisIterator iter = doc1.iterateAxis(Axis.CHILD);
                NodeInfo next = (NodeInfo) iter.next();

                while (next != null) {
                    AxisIterator iter2 = next.iterateAxis(Axis.CHILD);
                    Item next2 = iter2.next();

                    while (next2 != null) {
                        if (next2 instanceof NodeInfo && ((NodeInfo) next2).getNodeKind() == Type.ELEMENT) {
                            NodeInfo info = (NodeInfo) next2;

                            String nodeXml = ISO19139cheUtil.writeXml(info).replaceAll("LinearRing srsDimension=\"\\d\"", "LinearRing");
                            builder.append(nodeXml);
                        }
                        next2 = iter2.next();
                    }
                    next = (NodeInfo) iter.next();
                }

                builder.append("</gmd:polygon></gmd:EX_BoundingPolygon>");

                Source xmlSource = new StreamSource(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")));
                DocumentInfo doc = ni.getConfiguration().buildDocument(xmlSource);

                return SingletonIterator.makeIterator(doc);
            }
        });

    }

    public static Object combineAndWriteGeom(Object description, UnfailingIterator src, ISO19139cheUtil.GeomWriter writer) throws Exception {

        try {
            Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();

            NodeInfo next = (NodeInfo) src.next();

            while (next != null) {
                if (!next.getLocalPart().equalsIgnoreCase("geographicElement")) {
                    AxisIterator childNodes = next.iterateAxis(Axis.CHILD);

                    NodeInfo nextChild = (NodeInfo) childNodes.next();
                    while (nextChild != null) {
                        geoms.putAll(ISO19139cheUtil.geometries(nextChild));
                        nextChild = (NodeInfo) childNodes.next();
                    }

                }
                next = (NodeInfo) src.next();
            }

            GeometryFactory fac = new GeometryFactory();

            MultiPolygon inclusion = null;
            if (!geoms.get(true).isEmpty()) {
                inclusion = ExtentHelper.joinPolygons(fac, geoms.get(true));
            }
            MultiPolygon exclusion = null;
            if (!geoms.get(false).isEmpty()) {
                exclusion = ExtentHelper.joinPolygons(fac, geoms.get(false));
            }


            Object result;

            if (inclusion == null) {
                if (exclusion == null) {
                    result = src;
                } else {
                    result = writer.write(ExtentHelper.ExtentTypeCode.EXCLUDE, exclusion);
                }
            } else if (exclusion == null) {
                result = writer.write(ExtentHelper.ExtentTypeCode.INCLUDE, inclusion);
            } else {
                Pair<ExtentHelper.ExtentTypeCode, MultiPolygon> diff = ExtentHelper.diff(fac, inclusion, exclusion);
                result = writer.write(diff.one(), diff.two());;
            }

            return result;
        } catch (Throwable t) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document ownerDocument = impl.createDocument(null, null, null);
            Element root = ownerDocument.createElement("ERROR");
            root.setAttribute("msg", t.toString().replaceAll("\"", "'"));
            StackTraceElement[] trace = t.getStackTrace();
            for (StackTraceElement stackTraceElement : trace) {
                Element traceElem = ownerDocument.createElement("trace");
                traceElem.setTextContent(stackTraceElement.toString());
                root.appendChild(traceElem);
            }
            return root;
        }
    }
    static Multimap<Boolean, Polygon> geometries(NodeInfo next) throws Exception {
        Boolean inclusion = ISO19139cheUtil.inclusion(next);
        inclusion = inclusion == null ? Boolean.TRUE : inclusion;
        Polygon geom = ISO19139cheUtil.geom(next);
        Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();
        geoms.put(inclusion, geom);
        return geoms;
    }

    static Polygon geom(NodeInfo next) throws Exception {

        if ("Polygon".equals(next.getLocalPart())) {
            return ISO19139cheUtil.parsePolygon(next);
        }
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);
        NodeInfo curChildNode = (NodeInfo) childNodes.next();

        while (curChildNode != null) {
            Polygon geom = geom(curChildNode);
            if (geom != null) {
                return geom;
            }
            curChildNode = (NodeInfo) childNodes.next();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    static Polygon parsePolygon(NodeInfo next) throws Exception {
        String writeXml = ISO19139cheUtil.writeXml(next);

        Object value = gml3Parser().parse(new StringReader(writeXml));
        if (value instanceof HashMap) {
            HashMap map = (HashMap) value;
            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                // all I can think about is throwing up, from SpatialIndexWriter, addToList, historic
                if (entry instanceof Polygon) {
                    geoms.add((Polygon) entry);
                } else if (entry instanceof Collection) {
                    Collection collection = (Collection) entry;
                    for (Object object : collection) {
                        geoms.add((Polygon) object);
                    }
                }
            }
            if (geoms.isEmpty()) {
                return null;
            } else if (geoms.size() > 1) {
                throw new AssertionError("Some how multiple polygons were parsed");
            } else {
                return geoms.get(0);
            }

        } else if (value == null) {
            return null;
        } else {
            return (Polygon) value;
        }
    }

    static
    @Nullable
    Boolean inclusion(NodeInfo next) {
        if ("extentTypeCode".equals(next.getLocalPart())) {
            return booleanText(next);
        }
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);
        NodeInfo nextChild = (NodeInfo) childNodes.next();

        while (nextChild != null) {
            Boolean inclusion = inclusion(nextChild);
            if (inclusion != null) {
                return inclusion;
            }
            nextChild = (NodeInfo) childNodes.next();

        }
        return null;
    }

    static Boolean booleanText(NodeInfo next) {
        AxisIterator childNodes = next.iterateAxis(Axis.CHILD);

        NodeInfo nextChild = (NodeInfo) childNodes.next();

        while (nextChild != null) {
            if ("Boolean".equals(nextChild.getLocalPart())) {
                Item firstChild = nextChild.iterateAxis(Axis.CHILD).next();
                if (firstChild != null) {
                    String textContent = firstChild.getStringValue();
                    return "1".equals(textContent) || "true".equalsIgnoreCase(textContent);
                }
            }
            nextChild = (NodeInfo) childNodes.next();
        }
        return true;
    }


}
