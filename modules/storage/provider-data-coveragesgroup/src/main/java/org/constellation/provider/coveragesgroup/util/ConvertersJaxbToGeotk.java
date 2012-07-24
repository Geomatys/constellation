/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.coveragesgroup.util;

import java.util.logging.Logger;
import org.constellation.util.DataReference;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.EmptyMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.util.Converters;
import org.geotoolkit.util.logging.Logging;


/**
 *
 * @author Cédric Briançon
 */
public final class ConvertersJaxbToGeotk {
    private static final Logger LOGGER = Logging.getLogger(ConvertersJaxbToGeotk.class);

    public static MapItem convertsMapLayer(final org.geotoolkit.providers.xml.MapLayer mapLayer) {
        final String dataReference = mapLayer.getDataReference().getValue();
        final String layerName;
        final String providerName; // what to do with this ?


        DataReference ref = new DataReference(dataReference);

        Object obj = Converters.convert(ref, FeatureCollection.class);
        if(obj == null) obj = Converters.convert(ref, CoverageReference.class);


        if(obj instanceof FeatureCollection){
            final FeatureMapLayer layer = MapBuilder.createFeatureLayer((FeatureCollection)obj, new DefaultStyleFactory().style());
            return layer;

        }else if(obj instanceof CoverageReference){
            final CoverageMapLayer layer = MapBuilder.createCoverageLayer(
                    (CoverageReference)obj, new DefaultStyleFactory().style(), "");
            return layer;
        }

        final EmptyMapLayer emptyLayer = MapBuilder.createEmptyMapLayer();
        emptyLayer.setName(dataReference);
        return emptyLayer;
    }

    public static MapItem convertsMapItem(final org.geotoolkit.providers.xml.MapItem mapItem) {
        final MapItem mi = MapBuilder.createItem();
        for (org.geotoolkit.providers.xml.MapItem currentMapItem : mapItem.getMapItems()) {
            if (currentMapItem instanceof org.geotoolkit.providers.xml.MapLayer) {
                final MapItem layer = convertsMapLayer((org.geotoolkit.providers.xml.MapLayer)currentMapItem);
                if (layer != null) {
                    layer.setUserPropertie("original_config", currentMapItem);
                    mi.items().add(layer);
                }
            } else {
                mi.items().add(convertsMapItem(currentMapItem));
            }
        }
        return mi;
    }

    public static MapContext convertsMapContext(final org.geotoolkit.providers.xml.MapContext mapContext) {
        final MapContext mc = MapBuilder.createContext();
        mc.items().add(convertsMapItem(mapContext.getMapItem()));
        return mc;
    }
}
