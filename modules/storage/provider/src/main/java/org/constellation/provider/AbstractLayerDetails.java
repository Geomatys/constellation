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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.legend.DefaultLegendService;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.sld.MutableLayer;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;
import org.opengis.util.FactoryException;

/**
 * Abstract layer, handle name and styles.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
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
    public BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template,
                                          final String style) throws PortrayalException
    {
        MutableStyle mutableStyle = null;
        if (style != null && !style.isEmpty()) {
            mutableStyle = StyleProviderProxy.getInstance().get(style);
            if (mutableStyle == null) {
                LOGGER.info("The given style "+ style +" was not known. Use the default one instead.");
            }
        }
        return generateLegendGraphic(dimension, template, mutableStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template,
                                          final String sld, final StyledLayerDescriptor version,
                                          final String layerName) throws PortrayalException
    {
        MutableStyle mutableStyle = null;
        if (sld != null && !sld.isEmpty()) {
            // First ensures the URL is valid.
            final URL sldUrl;
            try {
                sldUrl = new URL(sld);
            } catch (MalformedURLException ex) {
                throw new PortrayalException("The given SLD url is not valid", ex);
            }
            // Obtain a SLD object from the URL response.
            final XMLUtilities utils = new XMLUtilities();
            final MutableStyledLayerDescriptor mutableSLD;
            try {
                mutableSLD = utils.readSLD(sldUrl, version);
            } catch (JAXBException ex) {
                throw new PortrayalException("The given SLD is not valid", ex);
            } catch (FactoryException ex) {
                throw new PortrayalException("The given SLD is not valid", ex);
            }

            final List<MutableLayer> layers = mutableSLD.layers();
            for (final MutableLayer layer : layers) {
                if (layerName.equals(layer.getName())) {
                    mutableStyle = (MutableStyle) layer.styles().get(0);
                    break;
                }
            }
            if (mutableStyle == null) {
                LOGGER.info("No layer "+ layerName +" found for the given SLD. Continue with the first style found.");
                mutableStyle = (MutableStyle) layers.get(0).styles().get(0);
            }
        }
        return generateLegendGraphic(dimension, template, mutableStyle);
    }

    /**
     * Generates the legend graphic output. If the {@code style} parameter is {@code null},
     * then a default one will be chosen.
     *
     * @param dimension
     * @param template
     * @param style
     * @return
     */
    private BufferedImage generateLegendGraphic(final Dimension dimension, final LegendTemplate template,
                                                MutableStyle style)
    {
        if (style == null) {
            if(!getFavoriteStyles().isEmpty()){
                style = StyleProviderProxy.getInstance().get(getFavoriteStyles().get(0));
            }
            if (style == null) {
                style = getDefaultStyle();
            }
        }

        try {
            final MapLayer layer = getMapLayer(style, null);
            final MapContext context = MapBuilder.createContext();
            context.layers().add(layer);
            return DefaultLegendService.portray(template, context, dimension);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
        }

        return DefaultGlyphService.create(style, dimension,null);
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
