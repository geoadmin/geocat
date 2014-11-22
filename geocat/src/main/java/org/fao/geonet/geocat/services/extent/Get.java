//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.geocat.services.extent;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.geocat.kernel.extent.ExtentFormat;
import org.fao.geonet.geocat.kernel.extent.ExtentHelper;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.extent.Source;
import org.fao.geonet.geocat.kernel.extent.FeatureType;
import org.fao.geonet.kernel.region.Region;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;


/**
 * Obtains the geometry and description from the wfs (configured in config.xml)
 * and returns them as a gmd:extent xml fragment
 *
 * @author jeichar
 */
public class Get implements Service
{

    private final GMLConfiguration gmlConfiguration = new GMLConfiguration();
    {
    	@SuppressWarnings("unchecked")
		Set<Object> props = gmlConfiguration.getProperties();
    	props.add(GMLConfiguration.NO_SRS_DIMENSION);
    }
    private Path _appPath;

    public Element exec(Element params, ServiceContext context) throws Exception
    {
        Util.toLowerCase(params);
        final ExtentManager extentMan = context.getBean(ExtentManager.class);

        final String id = Util.getParamText(params, ExtentHelper.ID);
        final String formatParam = Util.getParamText(params, ExtentHelper.FORMAT);
        final String typename = Util.getParamText(params, ExtentHelper.TYPENAME);
        final String extentTypeCode = Util.getParamText(params, ExtentHelper.EXTENT_TYPE_CODE);
        final String epsgCode = Util.getParamText(params, ExtentHelper.CRS_PARAM);
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        int coordDigits = ExtentHelper.COORD_DIGITS;
        if(epsgCode != null) {
            crs = Region.decodeCRS(epsgCode);
            if(epsgCode.contains("21781")) {
        	coordDigits = 0;
            }
        }

        ExtentFormat format = ExtentFormat.lookup(formatParam);

        if (id == null) {
            ExtentHelper.error("id parameter is required");
        }

        if (typename == null) {
            ExtentHelper.error("typename parameter is required");
        }
        final Source wfs = extentMan.getSource();
        final FeatureType featureType = wfs.getFeatureType(typename);
        if (featureType == null) {
            return errorTypename(extentMan, typename);
        }

        if (id==null || id.equals("SKIP") || id.length() == 0) {
            final Element response = new Element("response");
            ExtentFormat.formatFeatureType(featureType, wfs, response);
            return response;
        }

        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = featureType.getFeatureSource();

        String[] properties;
        if(featureSource.getSchema().getDescriptor(featureType.showNativeColumn) != null) {
            properties = new String[]{ featureType.idColumn, featureSource.getSchema().getGeometryDescriptor().getLocalName(),
                    featureType.descColumn, featureType.geoIdColumn, featureType.showNativeColumn };
        } else {
            properties = new String[]{ featureType.idColumn, featureSource.getSchema().getGeometryDescriptor().getLocalName(),
                    featureType.descColumn, featureType.geoIdColumn};
        }

        final FilterFactory2 filterFactory2 = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        Class<?> idBinding = featureType.getFeatureSource().getSchema().getDescriptor(featureType.idColumn).getType().getBinding();
        String finalId = id;
        try {
	        if(id.contains(".") && (Short.class.isAssignableFrom(idBinding) || Integer.class.isAssignableFrom(idBinding) || Long.class.isAssignableFrom(idBinding))) {
        		finalId = id.substring(0, id.indexOf('.'));
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        final Filter filter = filterFactory2.equals(filterFactory2.property(featureType.idColumn), filterFactory2
        		.literal(finalId));

        final Query q = featureType.createQuery(filter,properties);

        final Element xml = resolve(format, id, featureSource, q, featureType, wfs, extentTypeCode, crs, coordDigits);
        return xml;
    }

    private Element resolve(ExtentFormat format, String id, FeatureSource<SimpleFeatureType, SimpleFeature> featureSource,
            Query q, FeatureType featureType, Source wfs, String extentTypeCode, CoordinateReferenceSystem crs, int coordDigits) throws Exception, Exception
    {
        FeatureIterator<SimpleFeature> features = null;
        try {
        	features = featureSource.getFeatures(q).features();
            if (features.hasNext()) {
                final SimpleFeature feature = features.next();

                return format.format(gmlConfiguration, _appPath, feature, featureType, wfs, extentTypeCode, crs, coordDigits);
            } else {
                return ExtentHelper.error("no features founds with ID=" + id);
            }
        } finally {
        	if (features != null) {
        		features.close();
        	}
        }
    }

    private Element errorTypename(ExtentManager extentMan, String typename) throws IOException
    {
        final String options = Arrays.toString(extentMan.getDataStore().getTypeNames());
        final String msg = "Typename: " + typename + " does not exist.  Available options are: " + options;
        return ExtentHelper.error(msg);
    }

    public void init(Path appPath, ServiceConfig params) throws Exception
    {
        this._appPath = appPath;
    }

}
