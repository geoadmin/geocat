package org.fao.geonet.util;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.XLink;
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
import org.apache.commons.lang.SystemUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClients;
import org.fao.geonet.Constants;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.geocat.kernel.extent.ExtentHelper;
import org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.schema.iso19139che.ISO19139cheNamespaces;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.fao.geonet.utils.Xml;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.json.XML;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

import static jeeves.xlink.XLink.HREF;
import static jeeves.xlink.XLink.NAMESPACE_XLINK;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.ExtentTypeCode;
import static org.fao.geonet.geocat.kernel.reusable.KeywordsStrategy.NON_VALID_THESAURUS_NAME;
import static org.fao.geonet.geocat.kernel.reusable.ReusableObjManager.NON_VALID_ROLE;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;

public class GeocatXslUtil {
    public static final String NO_LANG = "NO_LANG";
    private static Pattern ID_EXTRACTOR = Pattern.compile(".*id=([^&]+).*");
    private static Pattern TYPENAME_EXTRACTOR = Pattern.compile(".*typename=([^&]+).*");

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

    public static UnfailingIterator parse(NodeInfo text)
            throws Exception {
        String resultString = "<div>" + text.getStringValue() + "</div>";

        try {
            Source xmlSource = new StreamSource(new ByteArrayInputStream(resultString.getBytes("UTF-8")));
            DocumentInfo doc = text.getConfiguration().buildDocument(xmlSource);
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


    private static final Cache<String, Boolean> URL_VALIDATION_CACHE;

    static {
        URL_VALIDATION_CACHE = CacheBuilder.<String, Boolean>newBuilder().
                maximumSize(100000).
                expireAfterAccess(25, TimeUnit.HOURS).
                build();
    }

    public static boolean validateURL(final String urlString) throws ExecutionException {
        return URL_VALIDATION_CACHE.get(urlString, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                try {
//            System.out.println("Testing url : " + urlString);
                    HttpHead head = new HttpHead(urlString);

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
                    RequestConfig builtRequestConfig = requestConfig.build();
                    head.setConfig(builtRequestConfig);

                    HttpResponse response = client.execute(head);

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        HttpHead get = new HttpHead(urlString);
                        get.setConfig(builtRequestConfig);

                        response = client.execute(head);

                        return response.getStatusLine().getStatusCode() != HttpStatus.SC_NOT_FOUND;
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    public static String parseRegionIdFromXLink(String xlink) {

        if (xlink != null && !xlink.isEmpty()) {
            String typeName = extractFromHref(xlink, TYPENAME_EXTRACTOR);
            String id = extractFromHref(xlink, ID_EXTRACTOR);
            if (typeName != null && id != null) {
                switch (typeName) {
                    case "gn:countries":
                        typeName = "country";
                        break;
                    case "gn:gemeindenBB":
                        typeName = "gemeinden";
                        break;
                    case "gn:kantoneBB":
                        typeName = "kantone";
                        break;
                    case "gn:xlinks":
                        typeName = "xlinks";
                        break;
                    case "gn:non_validated":
                        typeName = "non_validated";
                        break;
                    default:
                        typeName = null;
                }
                if (typeName != null) {
                    return typeName + ":" + id;
                }
            }
        }

        return null;
    }

    private static String extractFromHref(String xlink, Pattern pattern) {
        String typeName = null;
        Matcher matcher = pattern.matcher(xlink);
        if (matcher.matches()) {
            typeName = matcher.group(1);
        }
        return typeName;
    }

    /**
     * Merges the keywords in the metadata document by thesaurus so that all keywords from the same thesaurus are in the same
     * keyword block.
     * @param el the identification info element
     * @param removeEmpty if true then any empty descriptiveKeyword block (by empty I mean no keywords for that thesaurus) then remove
     *                    the block
     * @param thesaurusKeys should be null except for SharedObjects.  All the keywords in the system.  Used to check if a keyword is valid.
     * @param missingKeywords
     * @param wordToIdLookup
     */
    public static void mergeKeywords(
            org.jdom.Element el, boolean removeEmpty, Multimap<String, String> thesaurusKeys, Map<String,
            Pair<String, org.jdom.Element>> missingKeywords,
            Map<Pair<String, String>, String> wordToIdLookup) throws JDOMException {
        final List<Namespace> namespaces = Lists.newArrayList(ISO19139Namespaces.GMD);
        @SuppressWarnings("unchecked")
        final List<org.jdom.Element> keywordEls = Lists.newArrayList((List<org.jdom.Element>)
                Xml.selectNodes(el, "*/gmd:descriptiveKeywords|*/srv:keywords", namespaces));

        Multimap<String, Keyword> keywordsByThesaurus = LinkedHashMultimap.create();
        int index = Integer.MAX_VALUE;
        org.jdom.Element parent = null;
        for (org.jdom.Element keywordEl : keywordEls) {
            final int currentIndex = keywordEl.getParentElement().indexOf(keywordEl);
            if (currentIndex < index) {
                index = currentIndex;
                parent = keywordEl.getParentElement();
            }

            final String attributeValue = keywordEl.getAttributeValue(HREF, NAMESPACE_XLINK);
            if (attributeValue!= null && !attributeValue.trim().isEmpty()) {
                Keyword keyword = new Keyword(attributeValue);
                if (keyword.thesaurus != null && keyword.id != null && !keyword.id.isEmpty()) {
                    // remove after 3.0 is deployed into production
                    ensureKeywordExistsInThesauri(thesaurusKeys, wordToIdLookup, missingKeywords, keywordsByThesaurus, keywordEl, keyword);
                    if (!keyword.id.isEmpty()) {
                        if (keyword.thesaurus.equals(AllThesaurus.ALL_THESAURUS_KEY)) {
                            for (String id : keyword.id) {
                                AllThesaurus.DecomposedAllUri decomposed = new AllThesaurus.DecomposedAllUri(id);
                                String keywordId = null;
                                try {
                                    keywordId = URLEncoder.encode(decomposed.keywordUri, Constants.ENCODING);
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                                keywordsByThesaurus.put(decomposed.thesaurusKey,
                                        new Keyword(
                                                decomposed.thesaurusKey, Collections.singleton(keywordId)));
                            }
                        } else {
                            keywordsByThesaurus.put(keyword.thesaurus, keyword);
                        }
                    }
                }
            }
            keywordEl.detach();
        }

        if (parent != null) {
            for (String thesaurus : keywordsByThesaurus.keySet()) {
                Set<String> ids = Sets.newLinkedHashSet();
                for (Keyword keyword : keywordsByThesaurus.get(thesaurus)) {
                    if (keyword.id != null) {
                        for (String id : keyword.id) {
                            if (id != null && !id.trim().isEmpty()) {
                                ids.add(id);
                            }
                        }
                    }
                }

                boolean canRemoveEmpty = removeEmpty || thesaurus.equals(AllThesaurus.ALL_THESAURUS_KEY);
                if (!canRemoveEmpty || !ids.isEmpty()) {
                    String keywordIds = Joiner.on(',').join(ids);
                    String joinedLangs = "eng,ger,ita,fre,roh";
                    String href = "local://eng/xml.keyword.get?thesaurus=" + thesaurus + "&id=" + keywordIds +
                                  "&multiple=true&lang=" + joinedLangs + "&textgroupOnly=true&skipdescriptivekeywords=true";

                    org.jdom.Element descriptiveKeywords = new org.jdom.Element("descriptiveKeywords", GMD).
                            setAttribute(HREF, href, NAMESPACE_XLINK);
                    if (thesaurus.equalsIgnoreCase(KeywordsStrategy.NON_VALID_THESAURUS_NAME)) {
                        descriptiveKeywords.setAttribute(XLink.ROLE, NON_VALID_ROLE, NAMESPACE_XLINK);
                    }
                    parent.addContent(index, descriptiveKeywords);

                    index++;
                }
            }
        }
    }

    private static void ensureKeywordExistsInThesauri(Multimap<String, String> thesaurusKeys, Map<Pair<String, String>, String>
            wordToIdLookup, Map<String, Pair<String, org.jdom.Element>>
            missingKeywords, Multimap<String, Keyword> keywordsByThesaurus, org.jdom.Element keywordEl, Keyword keyword) {
        if (keywordsByThesaurus == null || missingKeywords == null) {
            return;
        }
        try {
            final Iterator<String> iterator = keyword.id.iterator();
            while (iterator.hasNext()) {
                String keywordId = iterator.next();
                if (thesaurusKeys != null) {
                    Collection<String> keywordsForThesaurus = thesaurusKeys.get(keyword.thesaurus);
                    String decodedId = URLDecoder.decode(keywordId, Constants.ENCODING);

                    if (keywordsForThesaurus == null || !keywordsForThesaurus.contains(decodedId)) {
                        if (keywordsForThesaurus != null && decodedId.startsWith("file:")) {
                            List<String> split = Arrays.asList(decodedId.split("/"));
                            boolean found = false;
                            for (int i = 1; !found && i < split.size(); i++) {
                                String idFrag = Joiner.on("/").join(split.subList(split.size() - i, split.size()));
                                if (keywordsForThesaurus.contains(idFrag)) {
                                    if (SystemUtils.IS_OS_WINDOWS) {
                                        iterator.remove();
                                        String[] thesPars = keyword.thesaurus.split("\\.");
                                        String path = System.getProperty("geonetwork.dir") + "/config/codelist/" + thesPars[0] +
                                                      "/thesauri/" + "/" + thesPars[1] + "/" + thesPars[2] + ".rdf";

                                        String newId = "file://" + Paths.get(path).toString() + "/" + idFrag;
                                        Keyword newKeyword = new Keyword(keyword.thesaurus, Lists.newArrayList(URLEncoder.encode(newId, Constants.ENCODING)));
                                        keywordsByThesaurus.put(keyword.thesaurus, newKeyword);
                                    }
                                    found = true;
                                }
                            }
                            if (found) {
                                continue;
                            }
                        }

                        if (missingKeywords.containsKey(keywordId)) {
                            String newId = missingKeywords.get(keywordId).one();
                            Keyword newKeyword = new Keyword(NON_VALID_THESAURUS_NAME, Lists.newArrayList(newId));
                            keywordsByThesaurus.put(NON_VALID_THESAURUS_NAME, newKeyword);
                            iterator.remove();
                        } else {
                            String locatedId = findKeyword(keyword, keywordEl, thesaurusKeys, wordToIdLookup);
                            if (locatedId != null) {
                                Keyword newKeyword = new Keyword(NON_VALID_THESAURUS_NAME, Lists.newArrayList(locatedId));
                                keywordsByThesaurus.put(NON_VALID_THESAURUS_NAME, newKeyword);
                                iterator.remove();
                            } else {
                                String newId = URLEncoder.encode(KeywordsStrategy.NAMESPACE + UUID.randomUUID(), Constants.ENCODING);
                                missingKeywords.put(keywordId, Pair.read(newId, keywordEl));
                                iterator.remove();
                                Keyword newKeyword = new Keyword(NON_VALID_THESAURUS_NAME, Lists.newArrayList(newId));
                                keywordsByThesaurus.put(NON_VALID_THESAURUS_NAME, newKeyword);
                            }
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String findKeyword(Keyword keyword, org.jdom.Element keywordEl, Multimap<String, String> thesaurusKeys,
                                      Map<Pair<String, String>, String> wordToIdLookup) {
        ArrayList<Namespace> namespaces = Lists.newArrayList(ISO19139Namespaces.GMD, ISO19139Namespaces.GCO);
        List<?> words = null;
        try {
            words = Xml.selectNodes(keywordEl, "*//gmd:keyword//gmd:LocalisedCharacterString", namespaces);

            Map<String, Integer> exactMatches = Maps.newHashMap();
            Map<String, Integer> backupMatches = Maps.newHashMap();

            for (Object wordObj : words) {
                org.jdom.Element wordEl = (org.jdom.Element) wordObj;
                String wd = wordEl.getTextTrim().toLowerCase();
                if (!wd.isEmpty()) {
                    String locale = wordEl.getAttributeValue("locale");
                    if (locale == null) {
                        locale = "";
                    } else {
                        locale = locale.substring(1).toLowerCase();
                    }

                    String id = wordToIdLookup.get(Pair.read(locale, wd));
                    if (id != null) {
                        addMatch(exactMatches, id);
                    }
                    id = wordToIdLookup.get(Pair.read(NO_LANG, wd));
                    if (id != null) {
                        addMatch(backupMatches, id);
                    }
                }
            }

            if (!exactMatches.isEmpty()) {
                return bestMatch(exactMatches);
            } else if (!backupMatches.isEmpty()) {
                return bestMatch(exactMatches);
            }

            return null;
        } catch (JDOMException e) {
            return null;
        }

    }

    private static String bestMatch(Map<String, Integer> exactMatches) {
        Map.Entry<String, Integer> bestMatch = null;

        for (Map.Entry<String, Integer> entry : exactMatches.entrySet()) {
            if (bestMatch == null || entry.getValue() > bestMatch.getValue()) {
                bestMatch = entry;
            }
        }
        if (bestMatch != null) {
            return bestMatch.getKey();
        }
        return null;
    }

    private static void addMatch(Map<String, Integer> exactMatches, String id) {
        Integer count = exactMatches.get(id);
        if (count == null) {
            count = 0;
        }

        exactMatches.put(id, count + 1);
    }

    private static final class Keyword {
        private final static Pattern PARAMS_PATTERN = Pattern.compile("(\\?|\\&)([^=]+)=([^\\&]+)");
        String thesaurus;
        Collection<String> id;
        final Set<String> langs = Sets.newHashSet();

        public Keyword(String thesaurus, Collection<String> id) {
            this.thesaurus = thesaurus;
            this.id = id;
        }

        public Keyword(String atValue) {
            String[] twoLetterLocales;
            final Matcher matcher = PARAMS_PATTERN.matcher(atValue.replace("&amp;", "&"));
            while(matcher.find()) {
                String key = matcher.group(2);
                String value = matcher.group(3);

                switch (key) {
                    case "thesaurus" :
                        thesaurus = value;
                        break;
                    case "id" :
                        id = Lists.newArrayList(value.split(","));
                        break;
                    case "lang" :
                        twoLetterLocales = value.split(",");
                        langs.addAll(Arrays.asList(twoLetterLocales));
                        break;
                    case "locales" :
                        twoLetterLocales = value.split(",");
                        for (int i = 0; i < twoLetterLocales.length; i++) {
                            String twoLetterLocale = twoLetterLocales[i];
                            if (twoLetterLocale.length() > 2) {
                                twoLetterLocale = twoLetterLocale.substring(0, 2);
                            }

                            switch (twoLetterLocale.toLowerCase()) {
                                case "de":
                                case "ge":
                                    twoLetterLocales[i] = "ger";
                                    break;
                                case "fr":
                                    twoLetterLocales[i] = "fre";
                                    break;
                                case "it":
                                    twoLetterLocales[i] = "ita";
                                    break;
                                case "en":
                                    twoLetterLocales[i] = "eng";
                                    break;
                                case "rm":
                                    twoLetterLocales[i] = "roh";
                                    break;
                                default:
                                    // skip
                            }
                        }

                        langs.addAll(Arrays.asList(twoLetterLocales));
                        break;
                    default:
                        break;
                }
            }
        }
    }
}