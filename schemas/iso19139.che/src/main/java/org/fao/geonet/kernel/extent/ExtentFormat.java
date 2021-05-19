package org.fao.geonet.kernel.extent;

import org.fao.geonet.Constants;
import org.fao.geonet.GeocatLangUtils;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.geometry.jts.JTS;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xsd.Encoder;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.fao.geonet.kernel.extent.ExtentHelper.reducePrecision;


/**
* Created with IntelliJ IDEA.
* User: Jesse
* Date: 11/15/13
* Time: 9:29 AM
* To change this template use File | Settings | File Templates.
*/
public enum ExtentFormat
{
    GMD_SPATIAL_EXTENT_POLYGON
    {
        @SuppressWarnings("unchecked")
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            Element fullObject = gmdFormat(gmlConfiguration, appPath, feature, GMD_POLYGON, featureType, extentTypeCode, crs,
                    coordPrecision);
            List<Element> geographicElements = new ArrayList<Element>(fullObject.getChildren("geographicElement",
                    ExtentManager.GMD_NAMESPACE));

            Element polygon, bbox, id;
            polygon = bbox = id = null;

            for (Element element : geographicElements) {
                element.detach();
                element.setName("spatialExtent");
                if (element.getChild("EX_BoundingPolygon", ExtentManager.GMD_NAMESPACE) != null) {
                    polygon = element.getChild("EX_BoundingPolygon", ExtentManager.GMD_NAMESPACE);
                } else if (element.getChild("EX_GeographicBoundingBox", ExtentManager.GMD_NAMESPACE) != null) {
                    bbox = element.getChild("EX_GeographicBoundingBox", ExtentManager.GMD_NAMESPACE);
                } else {
                    if (!element.getChildren().isEmpty()) {
                        id = (Element) element.getChildren().get(0);
                    }
                }
            }

            if (polygon != null)
                return polygon;
            if (bbox != null)
                return bbox;
            return id;
        }
    },
    GMD_BBOX
    {
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            return gmdFormat(gmlConfiguration, appPath, feature, this, featureType, extentTypeCode, crs, coordPrecision);
        }
    },
    GMD_POLYGON
    {
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            return gmdFormat(gmlConfiguration, appPath, feature, this, featureType, extentTypeCode, crs, coordPrecision);
        }
    },
    GMD_COMPLETE
    {
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            return gmdFormat(gmlConfiguration, appPath, feature, this, featureType, extentTypeCode, crs, coordPrecision);
        }
    },
    WKT
    {
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            return formatWKT(feature, featureType, wfs, crs, coordPrecision);
        }
    },
    AUTO
    {
        @Override
        public Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                              String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
        {
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (isSquare(geometry, coordPrecision)) {
                return GMD_BBOX.format(gmlConfiguration, appPath, feature, featureType, wfs, extentTypeCode, crs, coordPrecision);
            } else {
                return GMD_COMPLETE.format(gmlConfiguration, appPath, feature, featureType, wfs, extentTypeCode, crs, coordPrecision);
            }
        }
    };

    static boolean isSquare(Geometry geometry, int coordPrecision) {
        if (geometry.getNumPoints() == 5 && geometry.getNumGeometries() == 1
            && (geometry instanceof Polygon || geometry instanceof MultiPolygon)) {
            Geometry reducedPrecisionGeom = ExtentHelper.reducePrecision(geometry, coordPrecision);
            Coordinate[] coords = reducedPrecisionGeom.getCoordinates();

            for (int i = 1; i < coords.length; i++) {
                Coordinate coord0 = coords[i-1];
                Coordinate coord1 = coords[i];
                double xdiff = coord0.x - coord1.x;
                if (xdiff == 0) {
                    continue;
                }
                double slope = (coord0.y - coord1.y) / xdiff;
                if (!(eq(slope, 0) || eq(slope, 90) || eq(slope, 180) || eq(slope, 270))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private static boolean eq(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.0000001;
    }

    public abstract Element format(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, FeatureType featureType, Source wfs,
                                   String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision) throws Exception;

    public static ExtentFormat lookup(String formatParam)
    {
        ExtentFormat format;
        if (formatParam == null) {
            format = ExtentFormat.WKT;
        } else {
            format = ExtentFormat.valueOf(formatParam.toUpperCase());
        }
        return format;
    }


    private static Element gmdFormat(GMLConfiguration gmlConfiguration, Path appPath, SimpleFeature feature, ExtentFormat format,
                                     FeatureType featureType, String extentTypeCode, CoordinateReferenceSystem crs, int coordPrecision)
            throws Exception
    {

        final Element exExtent = new Element("EX_Extent", ExtentManager.GMD_NAMESPACE);
        final Element geographicElement = new Element("geographicElement", ExtentManager.GMD_NAMESPACE);

        exExtent.addContent(geographicElement);
        Element geoExTypeEl;
        switch (format)
        {
            case GMD_BBOX:
                geoExTypeEl = bbox(feature,crs);
                geographicElement.addContent(geoExTypeEl);
                addExtentTypeCode(geoExTypeEl, extentTypeCode);

                break;
            case GMD_COMPLETE:
                geoExTypeEl = boundingPolygon(gmlConfiguration, feature,crs, coordPrecision);
                geographicElement.addContent(geoExTypeEl);
                addExtentTypeCode(geoExTypeEl, extentTypeCode);

                final Element geographicElement2 = new Element("geographicElement", ExtentManager.GMD_NAMESPACE);
                exExtent.addContent(geographicElement2);
                Element bboxElem = bbox(feature,crs);
                geographicElement2.addContent(bboxElem);
                addExtentTypeCode(bboxElem, extentTypeCode);

                break;
            case GMD_POLYGON:
                geoExTypeEl = boundingPolygon(gmlConfiguration, feature,crs, coordPrecision);
                geographicElement.addContent(geoExTypeEl);
                addExtentTypeCode(geoExTypeEl, extentTypeCode);

                break;

            default:
                throw new IllegalArgumentException(format + " is not one of the permitted formats for this method");
        }

        String attribute = (String) feature.getAttribute(featureType.geoIdColumn);
        Element geoIdElem = createGeoIdElem(appPath, attribute);
        if (geoIdElem != null) {
            exExtent.addContent(0, geoIdElem);
        }

        try {
            Element descElem = null;
            attribute = (String) feature.getAttribute(featureType.descColumn);
            if (attribute != null) {
                descElem = GeocatLangUtils.toIsoMultiLingualElem(appPath, ExtentHelper.decodeDescription(attribute));
            }
            if (descElem == null) {
                // making a desc object always present. Mostly a hack to make
                // editing easier
                descElem = GeocatLangUtils.toIsoMultiLingualElem(appPath, " ");
            }
            exExtent.addContent(0, descElem);
        } catch (final Exception e) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out, true, Constants.ENCODING));
            Log.error("org.fao.geonet.services.xlink.Extent", "Error parsing XML from feature:\n" + out);
        }

        return exExtent;
    }

    private static void addExtentTypeCode(final Element parentEl, String extentTypeCode)
    {
        final Element typeCodeEl = new Element("extentTypeCode", ExtentManager.GMD_NAMESPACE);
        final Element booleanEl = new Element("Boolean", ExtentManager.GCO_NAMESPACE);

        parentEl.addContent(0, typeCodeEl);
        typeCodeEl.addContent(booleanEl);

        if (extentTypeCode == null || extentTypeCode.trim().length() == 0) {
            extentTypeCode = "true";
        }
        if(extentTypeCode.trim().equals("1")) {
            extentTypeCode = "true";
        }
        Boolean value = Boolean.parseBoolean(extentTypeCode.trim());
        booleanEl.setText(value?"1":"0");
    }

    @SuppressWarnings("unchecked")
    private static Element createGeoIdElem(Path appPath, String attribute) throws Exception
    {

        Element geoEl = new Element("geographicElement", ExtentManager.GMD_NAMESPACE);
        Element geoDesEl = new Element("EX_GeographicDescription", ExtentManager.GMD_NAMESPACE);
        Element geoIdEl = new Element("geographicIdentifier", ExtentManager.GMD_NAMESPACE);
        Element mdIdEl = new Element("MD_Identifier", ExtentManager.GMD_NAMESPACE);
        Element codeEl = new Element("code", ExtentManager.GMD_NAMESPACE);

        geoEl.addContent(geoDesEl);

        geoDesEl.addContent(geoIdEl);
        geoIdEl.addContent(mdIdEl);
        mdIdEl.addContent(codeEl);

        if (attribute != null && attribute.trim().length() > 0) {
            codeEl.setAttribute("type", "gmd:PT_FreeText_PropertyType", Namespace.getNamespace("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance"));
            String decodeDescription = ExtentHelper.decodeDescription(attribute);

            List<Content> content = new ArrayList<Content>(GeocatLangUtils.toIsoMultiLingualElem(appPath,
                    decodeDescription).getContent());
            for (Content element : content) {
                element.detach();
                codeEl.addContent(element);
            }
        } else {
            return null;
        }

        return geoEl;
    }

    private static Element bbox(SimpleFeature feature, CoordinateReferenceSystem crs) throws Exception
    {
        Element bbox = new Element("EX_GeographicBoundingBox", ExtentManager.GMD_NAMESPACE);
        Element west = new Element("westBoundLongitude", ExtentManager.GMD_NAMESPACE);
        Element east = new Element("eastBoundLongitude", ExtentManager.GMD_NAMESPACE);
        Element south = new Element("southBoundLatitude", ExtentManager.GMD_NAMESPACE);
        Element north = new Element("northBoundLatitude", ExtentManager.GMD_NAMESPACE);

        BoundingBox bounds = feature.getBounds();
        double eastDecimal = reducePrecision(bounds.getMaxX(), 0);
        double westDecimal = reducePrecision(bounds.getMinX(), 0);
        double southDecimal = reducePrecision(bounds.getMinY(), 0);
        double northDecimal = reducePrecision(bounds.getMaxY(), 0);

        bbox.addContent(new Comment(String.format("native coords: %s,%s,%s,%s", westDecimal, southDecimal, eastDecimal, northDecimal)));

        bbox.addContent(west);
        bbox.addContent(east);
        bbox.addContent(south);
        bbox.addContent(north);

        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        MathTransform transform = CRS.findMathTransform(feature.getFeatureType().getCoordinateReferenceSystem(), crs);
        Geometry transformed = JTS.transform(geometry, transform);

        Envelope latLongBounds = transformed.getEnvelopeInternal();
        double latLongEastDecimal = reducePrecision(latLongBounds.getMaxX(), ExtentHelper.COORD_DIGITS);
        double latLongWestDecimal = reducePrecision(latLongBounds.getMinX(), ExtentHelper.COORD_DIGITS);
        double latLongSouthDecimal = reducePrecision(latLongBounds.getMinY(), ExtentHelper.COORD_DIGITS);
        double latLongNorthDecimal = reducePrecision(latLongBounds.getMaxY(), ExtentHelper.COORD_DIGITS);

        west.addContent(decimal(latLongWestDecimal));
        east.addContent(decimal(latLongEastDecimal));
        south.addContent(decimal(latLongSouthDecimal));
        north.addContent(decimal(latLongNorthDecimal));

        return bbox;
    }

    private static Element decimal(double value)
    {
        Element dec = new Element("Decimal", ExtentManager.GCO_NAMESPACE);
        dec.setText(String.valueOf(value));
        return dec;
    }

    private static Element boundingPolygon(GMLConfiguration gmlConfiguration, SimpleFeature feature, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
    {
        final Element boundingPoly = new Element("EX_BoundingPolygon", ExtentManager.GMD_NAMESPACE);
        final Element polyon = new Element("polygon", ExtentManager.GMD_NAMESPACE);
        final Element geom = encodeAsGML(gmlConfiguration, feature, crs, coordPrecision);
        geom.detach();
        boundingPoly.addContent(polyon);
        polyon.addContent(geom);
        return boundingPoly;
    }

    private static Element encodeAsGML(GMLConfiguration gmlConfiguration, SimpleFeature feature, CoordinateReferenceSystem crs, int coordPrecision) throws Exception
    {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Encoder encoder = new Encoder(gmlConfiguration);
        encoder.setIndenting(false);
        final CoordinateReferenceSystem baseCrs = feature.getFeatureType().getCoordinateReferenceSystem();
        MathTransform transform = CRS.findMathTransform(baseCrs, crs, true);
        Geometry transformed = JTS.transform((Geometry) feature.getDefaultGeometry(), transform );
        reducePrecision(transformed, coordPrecision);

        transformed = removeDuplicatePoints(transformed);

        ExtentHelper.addGmlId(transformed);
        encoder.encode(transformed, org.geotools.gml3.GML.geometryMember, outputStream);
        String gmlString = outputStream.toString(Constants.ENCODING);
        Element geometryMembers = Xml.loadString(gmlString, false);
        @SuppressWarnings("rawtypes")
        Iterator iter = geometryMembers.getChildren().iterator();
        do {
            Object next = iter.next();
            if (next instanceof Element) {
                return (Element) next;
            }
        } while (iter.hasNext());

        throw new RuntimeException(transform+ " was not encoded correctly to GML");
    }

    static Geometry removeDuplicatePoints(Geometry geometry) {
        GeometryFactory factory = new GeometryFactory();

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            Polygon[] polygons = new Polygon[multiPolygon.getNumGeometries()];
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                polygons[i] = (Polygon) removeDuplicatePoints(multiPolygon.getGeometryN(i));
            }

            return factory.createMultiPolygon(polygons);
        } else if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;

            LinearRing shell = (LinearRing) removeDuplicatePoints(polygon.getExteriorRing());

            LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                holes[i] = (LinearRing) removeDuplicatePoints(polygon.getInteriorRingN(i));
            }

            return factory.createPolygon(shell, holes);
        } else if (geometry instanceof MultiLineString) {
            MultiLineString lineString = (MultiLineString) geometry;
            LineString[] lineStrings = new LineString[lineString.getNumGeometries()];
            for (int i = 0; i < lineString.getNumGeometries(); i++) {
                lineStrings[i] = (LineString) removeDuplicatePoints(lineString.getGeometryN(i));
            }

            return factory.createMultiLineString(lineStrings);
        } else if (geometry instanceof LinearRing) {
            CoordinateSequence coords = removeDuplicatePoints(((LineString) geometry).getCoordinateSequence());

            return factory.createLinearRing(coords);
        } else if (geometry instanceof LineString) {
            CoordinateSequence coords = removeDuplicatePoints(((LineString) geometry).getCoordinateSequence());

            return factory.createLineString(coords);
        } else {
            return geometry;
        }
    }

    private static CoordinateSequence removeDuplicatePoints(CoordinateSequence coordinateSequence) {
        if (coordinateSequence.size() == 0) {
            return coordinateSequence;
        }
        final Coordinate[] coordinates = new Coordinate[coordinateSequence.size()];

        int coords = 1;
        double x,y,z;
        x = coordinateSequence.getX(0);
        y = coordinateSequence.getY(0);
        z = coordinateSequence.getOrdinate(0, 2);
        coordinates[0] = coordinateSequence.getCoordinate(0);

        for (int i = 1; i < coordinates.length; i++) {
            double nx = coordinateSequence.getX(i);
            double ny = coordinateSequence.getY(i);
            double nz = coordinateSequence.getOrdinate(i, 2);

            if (Math.abs(x - nx) > 0.0000001 ||
                Math.abs(y - ny) > 0.0000001 ||
                Math.abs(z - nz) > 0.0000001) {
                coordinates[coords++] = coordinateSequence.getCoordinate(i);
            }

            x = nx;
            y = ny;
            z = nz;

        }

        if (coords < coordinates.length) {
            final Coordinate[] finalCoords = new Coordinate[coords];
            System.arraycopy(coordinates, 0, finalCoords, 0, coords);
            return new CoordinateArraySequence(finalCoords);
        }

        return coordinateSequence;
    }

    protected static Element formatWKT(SimpleFeature next, FeatureType featureType, Source wfs, CoordinateReferenceSystem crs, int coordDigits) throws Exception
    {
        final Element response = new Element("response");

        final Element featureTypeElem = formatFeatureType(featureType, wfs, response);

        final Element featureElem = new Element(ExtentHelper.FEATURE);
        final String id = next.getAttribute(featureType.idColumn).toString();
        featureElem.setAttribute(ExtentHelper.ID, id);
        featureTypeElem.addContent(featureElem);

        if (featureType.descColumn != null) {
            String desc = ExtentHelper.decodeDescription((String) next.getAttribute(featureType.descColumn));
            final Element descElem = Xml.loadString("<" + ExtentHelper.DESC + ">" + desc + "</" + ExtentHelper.DESC + ">", false);
            descElem.setAttribute("class", "object");
            featureElem.addContent(descElem);
        }

        if (featureType.geoIdColumn != null) {
            String desc = ExtentHelper.decodeDescription((String) next.getAttribute(featureType.geoIdColumn));
            final Element descElem = Xml.loadString("<" + ExtentHelper.GEO_ID + ">" + desc + "</" + ExtentHelper.GEO_ID + ">", false);
            descElem.setAttribute("class", "object");
            featureElem.addContent(descElem);
        }
        if (next.getDefaultGeometry() != null) {
            final Element geomElem = new Element(ExtentHelper.GEOM);
            final WKTWriter writer = new WKTWriter();
            Geometry geometry = (Geometry) next.getDefaultGeometry();
            MathTransform transform = CRS.findMathTransform(next.getFeatureType().getCoordinateReferenceSystem(), crs);
            Geometry transformed = JTS.transform(geometry, transform);
            final String wkt = writer.writeFormatted(reducePrecision(transformed, coordDigits));
            String openLayersCompatibleWKT = wkt.replaceAll("\\s+", " ");
            geomElem.setText(openLayersCompatibleWKT);
            featureElem.addContent(geomElem);
        }
        return response;

    }

    public static Element formatFeatureType(FeatureType featureType, Source wfs, Element response)
    {
        final Element wfsElem = new Element("wfs");
        wfsElem.setAttribute(ExtentHelper.ID, wfs.wfsId);
        response.addContent(wfsElem);

        final Element featureTypeElem = new Element(ExtentHelper.FEATURE_TYPE);
        featureTypeElem.setAttribute(ExtentHelper.TYPENAME, featureType.typename);
        featureTypeElem.setAttribute(ExtentHelper.ID_COLUMN, featureType.idColumn);
        featureTypeElem.setAttribute(ExtentHelper.DESC_COLUMN, featureType.descColumn);
        featureTypeElem.setAttribute(ExtentHelper.MODIFIABLE_FEATURE_TYPE, String.valueOf(featureType.isModifiable()));

        wfsElem.addContent(featureTypeElem);
        return featureTypeElem;
    }

}
