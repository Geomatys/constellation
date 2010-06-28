/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.provider.postgis;


import java.util.List;
import java.util.Map;

import org.constellation.provider.AbstractFeatureLayerDetails;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * PostGIS layer details.
 * 
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
class PostGisLayerDetails extends AbstractFeatureLayerDetails {

    PostGisLayerDetails(Name name, DataStore store, Name groupName, List<String> favorites){
        this(name,store, groupName,favorites,null,null,null,null);
    }
    
    PostGisLayerDetails(Name name, DataStore store, Name groupName, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        super(name,store, groupName,favorites,dateStart,dateEnd,elevationStart,elevationEnd);
    }

    @Override
    protected MapLayer createMapLayer(MutableStyle style, final Map<String, Object> params) throws DataStoreException {
        FeatureMapLayer layer = null;

        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            final String namedStyle = favorites.get(0);
            style = StyleProviderProxy.getInstance().get(namedStyle);
        }

        final FeatureType featureType = store.getFeatureType(groupName);
        if(style == null){
            //no favorites defined, create a default one
            style = RANDOM_FACTORY.createDefaultVectorStyle(featureType);
        }

        layer = MapBuilder.createFeatureLayer(store.createSession(false).getFeatureCollection(QueryBuilder.all(groupName)), style);
        
        layer.setElevationRange(elevationStartField, elevationEndField);
        layer.setTemporalRange(dateStartField, dateEndField);

        layer.setName(getName().getLocalPart());
        
        return layer;
    }

}
