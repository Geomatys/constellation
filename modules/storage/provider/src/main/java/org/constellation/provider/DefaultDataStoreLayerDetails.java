/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.provider;


import java.util.List;
import java.util.Map;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * Default layer details for a datastore type.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultDataStoreLayerDetails extends AbstractFeatureLayerDetails {

    public DefaultDataStoreLayerDetails(Name name, DataStore store, List<String> favorites){
        this(name,store,favorites,null,null,null,null);
    }

    public DefaultDataStoreLayerDetails(Name name, DataStore store, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        super(name,store,favorites,dateStart,dateEnd,elevationStart,elevationEnd);
    }

    @Override
    protected MapLayer createMapLayer(MutableStyle style, final Map<String, Object> params) throws DataStoreException {
        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            final String namedStyle = favorites.get(0);
            style = StyleProviderProxy.getInstance().get(namedStyle);
        }

        final FeatureType featureType = store.getFeatureType(name);
        if(style == null){
            //no favorites defined, create a default one
            style = RANDOM_FACTORY.createDefaultVectorStyle(featureType);
        }

        final FeatureMapLayer layer = MapBuilder.createFeatureLayer((FeatureCollection)getOrigin(), style);

        layer.setElevationRange(elevationStartField, elevationEndField);
        layer.setTemporalRange(dateStartField, dateEndField);

        final String title = getName().getLocalPart();
        layer.setName(title);
        layer.setDescription(STYLE_FACTORY.description(title,title));

        return layer;
    }

}
