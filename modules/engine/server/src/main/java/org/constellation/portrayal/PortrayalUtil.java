/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.portrayal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.constellation.provider.Data;

import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;


/**
 * Utility methods for the Portrayal system.
 * <p>
 * <b>Waring</b><br/>
 * None of these methods are currently used. This class is currently a
 * place holder and may be removed soon.
 * </p>
 *
 * @author Adrian Custer (Geomatys)
 * @since 0.3
 *
 */
public final class PortrayalUtil {


    public static MapContext createContext(Data layerRef, MutableStyle styleRef,
            Map<String,Object> renderingParameters) throws PortrayalException{
        return createContext(Collections.singletonList(layerRef),
                 Collections.singletonList(styleRef),
                 renderingParameters);

    }

    public static MapContext createContext(List<Data> layerRefs, List<MutableStyle> styleRefs,
            Map<String,Object> renderingParameters ) throws PortrayalException {

    	assert ( layerRefs.size() == styleRefs.size() );
        final MapContext context = MapBuilder.createContext();

        for (int i = 0; i < layerRefs.size(); i++) {
            final Data layerRef = layerRefs.get(i);
            final MutableStyle style = styleRefs.get(i);

            assert (null != layerRef);
            //style can be null

            final MapItem mapLayer = layerRef.getMapLayer(style, renderingParameters);
            if (mapLayer == null) {
                throw new PortrayalException("Could not create a mapLayer for layer: " + layerRef.getName());
            }
            if(mapLayer instanceof MapLayer){
                ((MapLayer)mapLayer).setSelectable(true);
            }
            mapLayer.setVisible(true);
            context.items().add(mapLayer);
        }

        return context;
    }

    //Don't allow instantiation
    private PortrayalUtil() {
    }
}
