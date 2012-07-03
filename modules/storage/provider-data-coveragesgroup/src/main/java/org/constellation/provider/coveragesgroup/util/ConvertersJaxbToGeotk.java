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
        final String providerName = mapLayer.getProviderReference().getValue();
        final LayerDetails ld = LayerProviderProxy.getInstance().getByIdentifier(new DefaultName(providerName));
        try {
            return ld.getMapLayer(null, null);
        } catch (PortrayalException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }
        return null;
    }

    public static MapItem convertsMapItem(final org.geotoolkit.providers.xml.MapItem mapItem) {
        final MapItem mi = MapBuilder.createItem();
        for (org.geotoolkit.providers.xml.MapItem currentMapItem : mapItem.getMapItems()) {
            if (currentMapItem instanceof org.geotoolkit.providers.xml.MapLayer) {
                mi.items().add(convertsMapLayer((org.geotoolkit.providers.xml.MapLayer)currentMapItem));
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
