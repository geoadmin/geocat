//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.geocat.services.extent;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.geocat.kernel.extent.ExtentHelper;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.extent.FeatureType;
import org.fao.geonet.geocat.kernel.extent.Source;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.FeatureStore;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;

import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.DESC;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.FORMAT;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.GEOM;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.GEO_ID;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.ID;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.TYPENAME;

/**
 * Service for adding new Geometries to a the updateable wfs featuretype
 *
 * @author jeichar
 */
public class Add implements Service {

    enum Format {
        WKT {
            @Override
            public Geometry parse(String geomParam) throws ParseException {
                final WKTReader reader = new WKTReader();
                return reader.read(geomParam);
            }

        },
        GML2 {

            transient Parser parser = new Parser(new GMLConfiguration());
            {
                parser.setFailOnValidationError(false);
                parser.setStrict(false);
                parser.setValidating(false);
            }

            @Override
            public Geometry parse(String geomParam) throws Exception {
                return gmlParsing(parser, geomParam);
            }

        },
        GML3 {

            transient Parser parser = new Parser(new org.geotools.gml3.GMLConfiguration());
            {
                parser.setFailOnValidationError(false);
                parser.setStrict(false);
                parser.setValidating(false);
            }

            @Override
            public Geometry parse(String geomParam) throws Exception {
                return gmlParsing(parser, geomParam);
            }

        };

        public static Format lookup(String param) {
            for (final Format format : values()) {
                if (format.name().equals(param)) {
                    return format;
                }
            }
            throw new IllegalArgumentException(param + " is not a recognized format.  Choices include: "
                                               + Arrays.toString(values()));
        }

        protected static Geometry gmlParsing(Parser parser, String gml) throws IOException, SAXException,
                ParserConfigurationException {
            Object obj = parser.parse(new StringReader(gml));

            if (obj instanceof Geometry) {
                return (Geometry) obj;
            }
            if (obj instanceof SimpleFeature) {
                return (Geometry) ((SimpleFeature) obj).getDefaultGeometry();
            }
            throw new AssertionError(obj.getClass().getName() + " was not an expected result from the Parser");
        }

        public abstract Geometry parse(String geomParam) throws Exception;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        final ExtentManager extentMan = context.getBean(ExtentManager.class);

        String id = Util.getParamText(params, ID);
        final String typename = Util.getParamText(params, TYPENAME);
        final String geomParam = Util.getParamText(params, GEOM);
        final String geomId = LangUtils.createDescFromParams(params, GEO_ID);
        final String desc = LangUtils.createDescFromParams(params, DESC);
        final Format format = Format.lookup(Util.getParamText(params, FORMAT));
        final String requestCrsCode = Util.getParamText(params, ExtentHelper.CRS_PARAM);

        final Source wfs = extentMan.getSource();
        final FeatureType featureType = wfs.getFeatureType(typename);

        if (featureType == null) {
            return ExtentHelper.error(typename + " does not exist, acceptable types are: " + wfs.listModifiable());
        }

        if (requestCrsCode == null) {
            return ExtentHelper.error("the " + ExtentHelper.CRS_PARAM + " parameter is required");
        }

        if (!featureType.isModifiable()) {
            return ExtentHelper.error(typename + " is not a modifiable type, modifiable types are: "
                                      + wfs.listModifiable());
        }
        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        if (id != null && idExists(store, id, featureType)) {
            return ExtentHelper.error("The id " + id + " already exists!");
        }

        Geometry geometry = format.parse(geomParam);
        id = extentMan.add(id, geomId, desc, requestCrsCode, featureType, store, geometry, false);

        final Element responseElem = new Element("success");
        responseElem.setText("Added one new feature id= " + id);
        return responseElem;
    }

    static boolean idExists(FeatureStore<SimpleFeatureType, SimpleFeature> store, String id, FeatureType featureType)
            throws IOException {
        return !store.getFeatures(featureType.createQuery(id, new String[]{featureType.idColumn})).isEmpty();
    }


}
