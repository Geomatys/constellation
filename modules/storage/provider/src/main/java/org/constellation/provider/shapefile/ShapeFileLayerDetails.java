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
package org.constellation.provider.shapefile;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.constellation.provider.AbstractFeatureLayerDetails;
import org.constellation.provider.StyleProviderProxy;

import org.geotools.data.FeatureSource;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
class ShapeFileLayerDetails extends AbstractFeatureLayerDetails {

    ShapeFileLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites){
        this(name,fs,favorites,null,null,null,null);
    }
    
    ShapeFileLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites,
        String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        super(name,fs,favorites,dateStart,dateEnd,elevationStart,elevationEnd);
    }
    
    @Override
    protected MapLayer createMapLayer(Object style, final Map<String, Object> params) throws IOException{
        MapLayer layer = null;

        if(style == null){
            //no style provided, try to get the favorite one
            if(favorites.size() > 0){
                //there are some favorites styles
                style = favorites.get(0);
            }else{
                //no favorites defined, create a default one
                style = RANDOM_FACTORY.createDefaultVectorStyle(fs);
            }
        }

        if(style instanceof String){
            //the given style is a named style
            style = StyleProviderProxy.getInstance().get((String)style);
            if(style == null){
                //somehting is wrong, the named style doesnt exist, create a default one
                style = RANDOM_FACTORY.createDefaultVectorStyle(fs);
            }
        }

        if(style instanceof MutableStyle){
            //style is a commun SLD style
            layer = MAP_BUILDER.createFeatureLayer(fs, (MutableStyle)style);
        }else if( style instanceof GraphicBuilder){
            //special graphic builder
            style = RANDOM_FACTORY.createDefaultVectorStyle(fs);
            layer = MAP_BUILDER.createFeatureLayer(fs, (MutableStyle)style);
            layer.graphicBuilders().add((GraphicBuilder) style);
        }else{
            //style is unknowed type, use a random style
            style = RANDOM_FACTORY.createDefaultVectorStyle(fs);
            layer = MAP_BUILDER.createFeatureLayer(fs, (MutableStyle)style);
        }

        if (params != null) {
            final Date date = (Date) params.get(KEY_TIME);
            final Number elevation = (Number) params.get(KEY_ELEVATION);
            layer.setQuery(createQuery(date, elevation));
        }

        layer.setName(getName());
        
        return layer;
    }
}
