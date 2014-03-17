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


import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.sis.storage.DataStoreException;

import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * Default layer details for a datastore type.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultDataStoreLayerDetails extends AbstractFeatureLayerDetails {

    /**
     * Build a FeatureLayerDetails with layer name, store and favorite style names.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     */
    public DefaultDataStoreLayerDetails(Name name, FeatureStore store, List<String> favorites){
        this(name,store,favorites,null,null,null,null,null);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names and data version date.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param versionDate data version date of the layer (can be null)
     */
    public DefaultDataStoreLayerDetails(Name name, FeatureStore store, List<String> favorites, Date versionDate){
        this(name,store,favorites,null,null,null,null, versionDate);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names and temporal/elevation filters.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param dateStart temporal filter start
     * @param dateEnd temporal filter end
     * @param elevationStart elevation filter start
     * @param elevationEnd elevation filter end
     */
    public DefaultDataStoreLayerDetails(Name name, FeatureStore store, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        this(name,store,favorites,dateStart,dateEnd,elevationStart,elevationEnd , null);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names, temporal/elevation filters and
     * data version date.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param dateStart temporal filter start
     * @param dateEnd temporal filter end
     * @param elevationStart elevation filter start
     * @param elevationEnd elevation filter end
     * @param versionDate data version date of the layer (can be null)
     */
    public DefaultDataStoreLayerDetails(Name name, FeatureStore store, List<String> favorites,
                                        String dateStart, String dateEnd, String elevationStart, String elevationEnd, Date versionDate){
        super(name,store,favorites,dateStart,dateEnd,elevationStart,elevationEnd, versionDate);
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
            style = RandomStyleBuilder.createDefaultVectorStyle(featureType);
        }

        final FeatureMapLayer layer = MapBuilder.createFeatureLayer((FeatureCollection)getOrigin(), style);

        final String title = getName().getLocalPart();
        layer.setName(title);
        layer.setDescription(StyleProviderProxy.STYLE_FACTORY.description(title,title));

        return layer;
    }

}
