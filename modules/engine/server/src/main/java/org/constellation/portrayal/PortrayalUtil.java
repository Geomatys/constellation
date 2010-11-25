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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.constellation.provider.LayerDetails;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableLayerStyle;
import org.geotoolkit.sld.MutableNamedLayer;
import org.geotoolkit.sld.MutableNamedStyle;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.style.MutableStyle;

import org.opengis.feature.type.Name;

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


    public static MapContext createContext(LayerDetails layerRef, MutableStyle styleRef,
            Map<String,Object> renderingParameters) throws PortrayalException{
        return createContext(Collections.singletonList(layerRef),
                 Collections.singletonList(styleRef),
                 renderingParameters);

    }

    public static MapContext createContext(List<LayerDetails> layerRefs, List<MutableStyle> styleRefs,
            Map<String,Object> renderingParameters ) throws PortrayalException {

    	assert ( layerRefs.size() == styleRefs.size() );
        final MapContext context = MapBuilder.createContext();

        for (int i = 0; i < layerRefs.size(); i++) {
            final LayerDetails layerRef = layerRefs.get(i);
            final MutableStyle style = styleRefs.get(i);

            assert (null != layerRef);
            //style can be null

            final MapLayer mapLayer = layerRef.getMapLayer(style, renderingParameters);
            if (mapLayer == null) {
                throw new PortrayalException("Could not create a mapLayer for layer: " + layerRef.getName());
            }
            mapLayer.setSelectable(true);
            mapLayer.setVisible(true);
            context.layers().add(mapLayer);
        }

        return context;
    }

    public static Object getStyleForLayer(final LayerDetails layerRef,
            final String styleName,
            final MutableStyledLayerDescriptor sld) {
        final Object style;

        if (sld != null) {
            //try to use the provided SLD
            style = extractStyle(layerRef.getName(), sld);
        } else if (styleName != null) {
            style = styleName;
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
        return style;
    }

    public static List<Object> getStylesForLayers(final List<LayerDetails> layerRefs,
            final List<String> styleNames,
            final MutableStyledLayerDescriptor sld) {

        final List<Object> styles = new ArrayList<Object>();

        for (int i = 0; i < layerRefs.size(); i++) {

            final Object style;

            if (sld != null) {
                //try to use the provided SLD
                style = extractStyle(layerRefs.get(i).getName(), sld);
            } else if (styles != null && styles.size() > i) {
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                style = styles.get(i);
            } else {
                //no defined styles, use the favorite one, let the layer get it himself.
                style = null;
            }

            styles.add(style);
        }
        return styles;
    }

    private static Object extractStyle(final Name layerName,
            final MutableStyledLayerDescriptor sld) {

        if (sld == null) {
            throw new IllegalArgumentException("SLD should not be null");
        }

        for (final MutableLayer layer : sld.layers()) {

            if (layer instanceof MutableNamedLayer && layerName.getLocalPart().equals(layer.getName())) {
                //we can only extract style from a NamedLayer that has the same name
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;

                for (final MutableLayerStyle mls : mnl.styles()) {
                    if (mls instanceof MutableNamedStyle) {
                        final MutableNamedStyle mns = (MutableNamedStyle) mls;
                        return mns.getName();
                    } else if (mls instanceof MutableStyle) {
                        return mls;
                    }

                }
            }
        }

        //no valid style found
        return null;
    }

    //Don't allow instantiation
    private PortrayalUtil() {
    }
}
