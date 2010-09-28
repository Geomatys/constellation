/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.legend.DefaultLegendService;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;
import org.opengis.style.Style;


/**
 * Abstract layer, handle name and styles.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractLayerDetails implements LayerDetails{

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

    /**
     * Favorites styles associated with this layer.
     */
    protected final List<String> favorites;

    /**
     * Layer name
     */
    protected final Name name;

    public AbstractLayerDetails(Name name, List<String> favorites){
        this.name = name;

        if(favorites == null){
            this.favorites = Collections.emptyList();
        }else{
            this.favorites = Collections.unmodifiableList(favorites);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Name getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFavoriteStyles() {
        return favorites;
    }

    /**
     * Returns the default style to apply for the renderer.
     */
    protected abstract MutableStyle getDefaultStyle();


    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(Dimension dimension, final LegendTemplate template,
                                          final Style style) throws PortrayalException
    {
        MutableStyle mutableStyle = null;
        if (style != null) {
            mutableStyle = (MutableStyle) style;
        } else {
            if (!getFavoriteStyles().isEmpty()) {
                mutableStyle = StyleProviderProxy.getInstance().get(getFavoriteStyles().get(0));
            }
            if (mutableStyle == null) {
                mutableStyle = getDefaultStyle();
            }
        }

        if (template == null) {
            if (dimension == null) {
                dimension = DefaultGlyphService.glyphPreferredSize(mutableStyle, dimension, null);
            }
            return DefaultGlyphService.create(mutableStyle, dimension, null);
        }
        try {
            final MapLayer layer = getMapLayer(mutableStyle, null);
            final MapContext context = MapBuilder.createContext();
            context.layers().add(layer);
            return DefaultLegendService.portray(template, context, dimension);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
        }

        if (dimension == null) {
            dimension = DefaultGlyphService.glyphPreferredSize(mutableStyle, dimension, null);
        }
        return DefaultGlyphService.create(mutableStyle, dimension, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredLegendSize(final LegendTemplate template, final MutableStyle ms) throws PortrayalException {
        final MapLayer ml = getMapLayer(ms, null);
        final MapContext mc = MapBuilder.createContext();
        mc.layers().add(ml);
        return DefaultLegendService.legendPreferredSize(template, mc);
    }

}
