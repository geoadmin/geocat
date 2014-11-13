package org.fao.geonet.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import jeeves.server.context.ServiceContext;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SingletonIterator;
import net.sf.saxon.om.UnfailingIterator;
import net.sf.saxon.type.Type;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClients;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.geocat.kernel.extent.ExtentHelper;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.jdom.Namespace;
import org.json.XML;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.ExtentTypeCode;

public class GeocatXslUtil {

    interface GeomWriter {
        Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception;
    }

    private static final GMLConfiguration GML3_CONFIG = new org.geotools.gml3.GMLConfiguration();

    static {
        @SuppressWarnings("unchecked")
        Set<QName> props = GeocatXslUtil.GML3_CONFIG.getProperties();
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

    /**
     * Convert 1E3 to 1000 for example
     */
    public static String expandScientific(Object src) {
        try {
            String original = src.toString().toUpperCase();
            String result = original;
            if (original.contains("E")) {
                String[] parts = original.split("E");
                double coefficient = Double.parseDouble(parts[0]);
                double exponentRaw = Double.parseDouble(parts[1]);
                double multiplier = Math.pow(10.0, exponentRaw);
                double expanded = coefficient * multiplier;
                result = String.valueOf(expanded);
            }
            return result;
        } catch (NumberFormatException e) {
            return src.toString();
        }
    }

    /**
     * convert gml geometry to WKT
     */
    public static String gmlToWKT(Node next) throws Exception {
        String writeXml = GeocatXslUtil.writeXml(next).replace("&lt;", "<").replace("&gt;", ">");
        if (writeXml.startsWith("<?xml")) {
            writeXml = writeXml.substring(writeXml.indexOf('>') + 1);
        }

        // make sure gml namespace is present since it sometimes gets dropped
        String firstTag = writeXml.substring(0, writeXml.indexOf('>'));
        if (!firstTag.contains("xmlns:gml")) {
            writeXml = firstTag + " xmlns:gml=\"http://www.opengis.net/gml\">" + writeXml.substring(firstTag.length() + 1);
        }

        Object value;
        try {
            value = gml3Parser().parse(new StringReader(writeXml));
        } catch (Exception e) {
            try {
                value = gml2Parser().parse(new StringReader(writeXml));
            } catch (Exception e2) {
                Log.error(Log.WEBAPP, "Unable to parse gml:" + writeXml + " problem: " + e2.getMessage());
                return "";
            }
        }
        Geometry geom = null;
        if (value instanceof HashMap) {

            // This section is unlikely more likely is a single polygon or multipolygon but this is for completeness

            @SuppressWarnings("rawtypes")
            HashMap map = (HashMap) value;

            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                SpatialIndexWriter.addToList(geoms, entry);
            }

            if (geoms.isEmpty()) {
                geom = null;
            } else if (geoms.size() > 1) {
                GeometryFactory fac = geoms.get(0).getFactory();
                geom = fac.buildGeometry(geoms);
            } else {
                geom = geoms.get(0);
            }

        } else if (value == null) {
            geom = null;
        } else {
            geom = (Geometry) value;
        }

        if (geom == null) {
            return "";
        } else {
            return new WKTWriter().write(geom).replaceAll("\\s+", " "); // replace all is to work around bugs in open layers
        }

    }

    public static String trimPosList(Object coords) {
        String[] coordsString = coords.toString().split(" ");

        StringBuilder results = new StringBuilder();

        for (int i = 0; i < coordsString.length; i++) {
            if (i > 0) {
                results.append(' ');
            }
            results.append(GeocatXslUtil.reduceDecimals(coordsString[i]));
        }

        return results.toString();
    }

    static String reduceDecimals(String number) {
        int DECIMALS = 6;
        try {
            // verify this is a number
            Double.parseDouble(number);

            String[] parts = number.split("\\.");

            if (parts.length > 1 && parts[1].length() > DECIMALS) {
                return parts[0] + '.' + parts[1].substring(0, DECIMALS);
            } else {
                return number;
            }
        } catch (Exception e) {
            return number;
        }
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

    public static String randomId() {
        return "N" + RANDOM.nextInt(Integer.MAX_VALUE);
    }

    public static Object bbox(Object description, Object src) throws Exception {

        final NodeInfo ni = (NodeInfo) src;
        return GeocatXslUtil.combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new GeocatXslUtil.GeomWriter() {

            public Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception {

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

                String extentTypeCode = code == ExtentTypeCode.EXCLUDE ? "false" : "true";
                String xml = String.format(template, extentTypeCode, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());

                Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                DocumentInfo doc = ni.getConfiguration().buildDocument(source);
                return SingletonIterator.makeIterator(doc);
            }
        });
    }

    public static Object multipolygon(Object description, Object src) throws Exception {

        final NodeInfo ni = ((NodeInfo) src);
        return GeocatXslUtil.combineAndWriteGeom(description, SingletonIterator.makeIterator(ni), new GeocatXslUtil.GeomWriter() {

            public Object write(ExtentTypeCode code, MultiPolygon geometry) throws Exception {
                geometry.setUserData(null);
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final Encoder encoder = new Encoder(GML3_CONFIG);
                encoder.setIndenting(false);
                encoder.setOmitXMLDeclaration(true);
                encoder.setEncoding(Charset.forName("UTF-8"));
                ExtentHelper.addGmlId(geometry);
                encoder.encode(geometry, org.geotools.gml3.GML.geometryMember, outputStream);

                StringBuilder builder = new StringBuilder("<gmd:EX_BoundingPolygon xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"><gmd:extentTypeCode><gco:Boolean>").
                        append(code == ExtentTypeCode.EXCLUDE ? "false" : "true").
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

                            String nodeXml = GeocatXslUtil.writeXml(info).replaceAll("LinearRing srsDimension=\"\\d\"", "LinearRing");
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

    public static Object combineAndWriteGeom(Object description, UnfailingIterator src, GeocatXslUtil.GeomWriter writer) throws Exception {

        try {
            Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();

            NodeInfo next = (NodeInfo) src.next();

            while (next != null) {
                if (!next.getLocalPart().equalsIgnoreCase("geographicElement")) {
                    AxisIterator childNodes = next.iterateAxis(Axis.CHILD);

                    NodeInfo nextChild = (NodeInfo) childNodes.next();
                    while (nextChild != null) {
                        geoms.putAll(GeocatXslUtil.geometries(nextChild));
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
                    result = writer.write(ExtentTypeCode.EXCLUDE, exclusion);
                }
            } else if (exclusion == null) {
                result = writer.write(ExtentTypeCode.INCLUDE, inclusion);
            } else {
                Pair<ExtentTypeCode, MultiPolygon> diff = ExtentHelper.diff(fac, inclusion, exclusion);
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
        Boolean inclusion = GeocatXslUtil.inclusion(next);
        inclusion = inclusion == null ? Boolean.TRUE : inclusion;
        Polygon geom = GeocatXslUtil.geom(next);
        Multimap<Boolean, Polygon> geoms = ArrayListMultimap.create();
        geoms.put(inclusion, geom);
        return geoms;
    }

    static Polygon geom(NodeInfo next) throws Exception {

        if ("Polygon".equals(next.getLocalPart())) {
            return GeocatXslUtil.parsePolygon(next);
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
        String writeXml = GeocatXslUtil.writeXml(next);

        Object value = gml3Parser().parse(new StringReader(writeXml));
        if (value instanceof HashMap) {
            HashMap map = (HashMap) value;
            List<Polygon> geoms = new ArrayList<Polygon>();
            for (Object entry : map.values()) {
                SpatialIndexWriter.addToList(geoms, entry);
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

    static Pattern LINK_PATTERN = Pattern.compile("(mailto:|https://|http://|ftp://|ftps://)[^\\s<>]*\\w");
    static Pattern NODE_PATTERN = Pattern.compile("<.+?>");

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
            return out.toString("utf-8").replaceFirst("<\\?xml.+?>", "");
        } catch (Throwable e) {
            return doc.getStringValue();
        }
    }

    /**
     * For all text split the lines to a specified size and add hyperlinks when appropriate
     */
    public static Object toHyperlinks(NodeInfo text) throws Exception {

        String textString = writeXml(text.getRoot());

        String linked = toHyperlinksSplitNodes(textString, text.getConfiguration());

        if (linked.equals(textString)) {
            return text;
        }
        Object nodes = parse(text.getConfiguration(), linked, true);

        if (nodes == null) {
            return text;
        } else {
            return nodes;
        }
    }

    /**
     * Sometimes nodes can have urls in their attributes (namespace declarations)
     * So nodes themselves should not be processed.  Also if a node is a
     * anchor node then the text within should not be processed.
     *
     * @param configuration
     */
    public static String toHyperlinksSplitNodes(String textString, Configuration configuration) throws Exception {
        Matcher nodes = NODE_PATTERN.matcher(textString);
        if (nodes.find()) {
            StringBuilder builder = new StringBuilder();
            boolean inAnchor = false;
            int i = 0;
            do {
                String beforeNode = textString.substring(i, nodes.start());
                if (inAnchor) {
                    // node is an anchor so just break lines
                    builder.append(insertBR(beforeNode));
                } else {
                    builder.append(toHyperlinksFromText(configuration, beforeNode));
                }

                if (!nodes.group().startsWith("<?xml")) {
                    builder.append(nodes.group());
                }

                if (nodes.group().startsWith("<a ")) {
                    // entering an anchor

                    inAnchor = true;
                }
                if (inAnchor && nodes.group().matches("</\\s*a\\s+.*")) {
                    // exiting anchor
                    inAnchor = false;
                }

                i = nodes.end();
            } while (nodes.find());

            builder.append(toHyperlinksFromText(configuration, textString.substring(i)));

            return builder.toString();
        } else {
            return toHyperlinksFromText(configuration, textString);
        }
    }

    /**
     * Add hyperlinks and split long lines
     *
     * @param configuration
     */
    static String toHyperlinksFromText(Configuration configuration, String textString) throws Exception {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        Matcher matcher = LINK_PATTERN.matcher(textString);


        if (!matcher.find()) return textString;

        do {
            builder.append(insertBR(textString.substring(i, matcher.start())));

            String tag = "<a href=\"" + matcher.group() + "\" target=\"_newtab\">" + insertBR(matcher.group()) + "</a>";

            // do a test to make sure the new text makes a valid document
            if (parse(configuration, builder.toString() + tag + textString.substring(matcher.end()), false) != null) {
                builder.append(tag);
            } else {
                builder.append(insertBR(textString.substring(matcher.start(), matcher.end())));
            }
            i = matcher.end();
        } while (matcher.find());

        builder.append(insertBR(textString.substring(i, textString.length())));

        return builder.toString();
    }

    /**
     * Insert line breaks
     */
    static String insertBR(String word) {

        Matcher nodes = NODE_PATTERN.matcher(word);

        if (nodes.find()) {
            StringBuilder b = new StringBuilder();
            int i = 0;
            do {
                b.append(insertBR(word.substring(i, nodes.start())));
                b.append(nodes.group());
                i = nodes.end();
            } while (nodes.find());
            return b.toString();
        }

        return word;
    }

    public static UnfailingIterator parse(Configuration configuration, String string, boolean printError)
            throws Exception {
        String resultString = "<div>" + string + "</div>";

        try {
            Source xmlSource = new StreamSource(new ByteArrayInputStream(resultString.getBytes("UTF-8")));
            DocumentInfo doc = configuration.buildDocument(xmlSource);
            return SingletonIterator.makeIterator(doc);
        } catch (Exception e) {
            org.jdom.Element error = JeevesException.toElement(e);
            Log.warning(Log.SERVICE, e.getMessage() + XML.toString(error));
            return null;
        }

    }

    public static String loadTranslationFile(Object filePattern, Object language) throws IOException {
        if (filePattern != null) {
            final ServiceContext serviceContext = ServiceContext.get();
            if (serviceContext != null) {
                final Charset charset = Charset.forName("UTF-8");

                String desiredPath = String.format(filePattern.toString(), XslUtil.twoCharLangCode(language.toString()));
                URL resource = serviceContext.getServlet().getServletContext().getResource(desiredPath);
                if (resource != null) {
                    return Resources.toString(resource, charset);
                }
                String defaultPath = String.format(filePattern.toString(), "en");
                resource = serviceContext.getServlet().getServletContext().getResource(defaultPath);
                if (resource != null) {
                    return Resources.toString(resource, charset);
                }
            }
        }

        return "{}";
    }


    public static boolean validateURL(String urlString) {
        try {
            System.out.println("Testing url : " + urlString);
            HttpHead method = new HttpHead(urlString);

            HttpClient client = HttpClients.createDefault();

            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            final RequestConfig.Builder requestConfig = RequestConfig.custom();
            requestConfig.setRedirectsEnabled(true);
            requestConfig.setConnectTimeout(10000);

            // Added support for proxy
            if (proxyHost != null && proxyPort != null) {
                requestConfig.setProxy(new HttpHost(proxyHost, Integer.valueOf(proxyPort)));
            }
            method.setConfig(requestConfig.build());

            final HttpResponse response = client.execute(method);

            return response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND;
        } catch (Exception e) {
            return false;
        }
    }
}