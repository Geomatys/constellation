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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
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

        int index = dataReference.lastIndexOf(":");
        if (index != -1) {
            layerName = dataReference.substring(index + 1);
            providerName = dataReference.substring(0, index);
        } else {
            throw new IllegalArgumentException("data reference must contain a ':' separator.");
        }
        final LayerDetails ld = LayerProviderProxy.getInstance().getByIdentifier(new DefaultName(layerName));
        if (ld != null) {
            try {
                return ld.getMapLayer(null, null);
            } catch (PortrayalException e) {
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            }
        } else {
            LOGGER.warning("unable to find a layer named:" + layerName);
        }
        return null;
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
