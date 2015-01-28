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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.geocat.kernel.extent.ExtentHelper;
import org.fao.geonet.geocat.kernel.extent.ExtentManager;
import org.fao.geonet.geocat.kernel.extent.ExtentSelection;
import org.fao.geonet.geocat.kernel.extent.FeatureType;
import org.fao.geonet.geocat.kernel.extent.Source;
import org.fao.geonet.geocat.kernel.reusable.ReusableTypes;
import org.fao.geonet.geocat.services.reusable.Reject;
import org.fao.geonet.util.LangUtils;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.ID;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.SELECTION;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.TYPENAME;
import static org.fao.geonet.geocat.kernel.extent.ExtentHelper.getSelection;

/**
 * Service for deleting extent objects from the WMS
 *
 * @author jeichar
 */
public class Delete implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        final ExtentManager extentMan = context.getBean(ExtentManager.class);
        boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
        final String selection = Util.getParamText(params, SELECTION);

        if (selection != null && Boolean.parseBoolean(selection)) {
            return deleteSelection(params, getSelection(context), extentMan, context);
        }

        if (!Boolean.parseBoolean(Util.getParam(params, "forceDelete", "false"))) {
            final String id = Util.getParamText(params, ID);
            String msg = LangUtils.loadString("reusable.rejectDefaultMsg", context.getAppPath(), context.getLanguage());
            boolean isValidated = !Util.getParamText(params, TYPENAME).contains("non_validated");
            return new Reject().reject(context, ReusableTypes.extents, new String[]{id}, msg, "", null, isValidated, testing);
        } else {
            return deleteSingle(params, extentMan);
        }
    }

    private Element deleteSingle(Element params, final ExtentManager extentMan) throws Exception {
        final String id = Util.getParamText(params, ID);
        final String typename = Util.getParamText(params, TYPENAME);

        final Source wfs = extentMan.getSource();
        final FeatureType featureType = wfs.getFeatureType(typename);
        if (featureType == null) {
            return ExtentHelper.error(typename + " does not exist, acceptable types are: " + wfs.listModifiable());
        }
        if (!featureType.isModifiable()) {
            return errorNotModifiable(featureType);
        }
        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureType
                .getFeatureSource();

        store.removeFeatures(featureType.createFilter(id));

        final Element responseElem = new Element("success");
        responseElem.setText("Removed features with id= " + id);
        return responseElem;
    }

    private Element errorNotModifiable(FeatureType ft) {
        return ExtentHelper.error(ft.typename + " is not a modifiable type, modifiable types are: "
                                  + ft.wfs().listModifiable());
    }

    private Element deleteSelection(Element params, ExtentSelection selection, ExtentManager extentMan,
                                    ServiceContext context) throws Exception {
        boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
        Element element = new Element("success");

        FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        Set<String> ids = new HashSet<String>();

        FeatureType currentType = null;

        final Set<Pair<FeatureType, String>> selectionIds = selection.getIds();
        synchronized (selectionIds) {
            @SuppressWarnings("unchecked")
            Pair<FeatureType, String>[] array = selectionIds.toArray(new Pair[selectionIds.size()]);
            Arrays.sort(array, new Comparator<Pair<FeatureType, String>>() {

                public int compare(Pair<FeatureType, String> o1, Pair<FeatureType, String> o2) {
                    return o1.one().typename.compareTo(o2.two());
                }

            });

            for (Pair<FeatureType, String> id : array) {
                FeatureType featureType = id.one();

                if (!featureType.isModifiable()) {
                    return errorNotModifiable(featureType);
                }

                if (!featureType.equals(currentType)) {
                    if (currentType != null) {
                        doDelete(filterFactory, currentType, ids, context, testing);
                    }
                    ids = new HashSet<String>();
                    currentType = featureType;
                }

                ids.add(id.two());
            }
            if (currentType != null) {
                doDelete(filterFactory, currentType, ids, context, testing);
            }
            ids.clear();
            element.setText("Deleted " + array.length + " extents");
        }

        return element;
    }

    private void doDelete(FilterFactory2 filterFactory, FeatureType currentType, Set<String> ids, ServiceContext context, boolean testing)
            throws Exception {

        String msg = "";// TODO
        boolean isValidated = !currentType.typename.contains("non_validated");
        new Reject().reject(context, ReusableTypes.extents, ids.toArray(new String[0]), msg, "", currentType.typename, isValidated, testing);
        // java.util.List<Filter> filters = new ArrayList<Filter>();
        // for (String id : ids) {
        // filters.add(currentType.createFilter(id));
        // }
        // FeatureStore<SimpleFeatureType, SimpleFeature> featureSource =
        // (FeatureStore) currentType.getFeatureSource();
        // featureSource.removeFeatures(filterFactory.or(filters));
    }

}
