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

package org.fao.geonet.kernel.extent;

import org.geotools.data.DataStore;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

public class Source {

    Set<FeatureType>          modifiable         = new LinkedHashSet<FeatureType>();
    Map<String, FeatureType>  types              = new LinkedHashMap<String, FeatureType>();
    public String             wfsId;

    @Autowired
    protected DataStore datastore;

    @PostConstruct
    protected void init() {
        for (FeatureType featureType : types.values()) {
            featureType.setSource(this);
        }
    }

    public void setModifiable(Set<FeatureType> modifiable) {
        this.modifiable = modifiable;
    }

    public void setTypes(Map<String, FeatureType> types) {
        this.types = types;
    }

    public void setWfsId(String wfsId) {
        this.wfsId = wfsId;
    }

    public synchronized DataStore getDataStore() throws IOException
    {
        return datastore;
    }

    @Override
    public String toString()
    {
        return wfsId;
    }

    public FeatureType getFeatureType(String typename)
    {
        if(types.containsKey(typename))
            return types.get(typename);
        else if(typename.startsWith("gn:") && types.containsKey(typename.substring(3))) {
            return types.get(typename.substring(3));
        } else {
            return null;
        }
    }

    public Map<String, FeatureType> getTypeDefinitions() {
        return types;
    }

    public Collection<FeatureType> getFeatureTypes() {
        return types.values();
    }

    public Collection<FeatureType> getModifiableTypes()
    {
        return modifiable;
    }

    public String listModifiable()
    {
        final List<String> name = new ArrayList<String>();
        for (final FeatureType type : getModifiableTypes()) {
            name.add(type.typename);
        }
        return name.toString();
    }

}
