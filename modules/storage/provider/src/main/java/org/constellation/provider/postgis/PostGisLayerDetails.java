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


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.constellation.provider.AbstractFeatureLayerDetails;
import org.constellation.provider.StyleProviderProxy;

import org.geotools.data.FeatureSource;
import org.geotools.map.MapBuilder;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * PostGIS layer details.
 * 
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
class PostGisLayerDetails extends AbstractFeatureLayerDetails {

    PostGisLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites){
        this(name,fs,favorites,null,null,null,null);
    }
    
    PostGisLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        super(name,fs,favorites,dateStart,dateEnd,elevationStart,elevationEnd);
    }

    @Override
    protected MapLayer createMapLayer(MutableStyle style, final Map<String, Object> params) throws IOException{
        MapLayer layer = null;

        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            String namedStyle = favorites.get(0);
            style = StyleProviderProxy.getInstance().get(namedStyle);
        }

        if(style == null){
            //no favorites defined, create a default one
            style = RANDOM_FACTORY.createDefaultVectorStyle(fs);
        }

        layer = MapBuilder.createFeatureLayer(fs, style);
        
        if (params != null) {
            final Date date = (Date) params.get(KEY_TIME);
            final Number elevation = (Number) params.get(KEY_ELEVATION);
            layer.setQuery(createQuery(date, elevation));
        }

        layer.setName(getName());
        
        return layer;
    }

}
