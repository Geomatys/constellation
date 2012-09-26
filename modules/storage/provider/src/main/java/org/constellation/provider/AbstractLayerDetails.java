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
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ServiceDef.Query;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.legend.DefaultLegendService;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.DateRange;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.Style;


/**
 * Abstract layer, handle name and styles.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractLayerDetails implements LayerDetails{

    protected static final Logger LOGGER = Logging.getLogger(AbstractLayerDetails.class);

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

    @Override
    public Object getOrigin(){
        return null;
    }

    /**
     * Returns the time range of this layer. The default implementation invoked
     * {@link #getAvailableTimes()} and extract the first and last date from it.
     * Subclasses are encouraged to provide more efficient implementation.
     */
    @Override
    public DateRange getDateRange() throws DataStoreException {
        final SortedSet<Date> dates = getAvailableTimes();
        if (dates != null && !dates.isEmpty()) {
            return new DateRange(dates.first(), dates.last());
        }
        return null;
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
                                          final Style style, final String rule, final Double scale)
                                          throws PortrayalException
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

        final MapItem mapItem = getMapLayer(mutableStyle, null);

        if(!(mapItem instanceof MapLayer)){
            //we can't render a glyph for a muli-layer
            return DefaultLegendService.portray(template, mapItem, dimension);
        }

        final MapLayer maplayer = (MapLayer) mapItem;

        if (template == null) {
            if (dimension == null) {
                dimension = DefaultGlyphService.glyphPreferredSize(mutableStyle, dimension, null);
            }
            // If a rule is given, we try to find the matching one in the style.
            // If none matches, then we can just apply all the style.
            if (rule != null) {
                final MutableRule mr = findRuleByNameInStyle(rule, mutableStyle);
                if (mr != null) {
                    return DefaultGlyphService.create(mr, dimension, maplayer);
                }
            }
            // Otherwise, if there is a scale, we can filter rules.
            if (scale != null) {
                final MutableRule mr = findRuleByScaleInStyle(scale, mutableStyle);
                if (mr != null) {
                    return DefaultGlyphService.create(mr, dimension, maplayer);
                }
            }
            return DefaultGlyphService.create(mutableStyle, dimension, maplayer);
        }
        try {
            return DefaultLegendService.portray(template, mapItem, dimension);
        } catch (PortrayalException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
        }

        if (dimension == null) {
            dimension = DefaultGlyphService.glyphPreferredSize(mutableStyle, dimension, null);
        }
        // If a rule is given, we try to find the matching one in the style.
        // If none matches, then we can just apply all the style.
        if (rule != null) {
            final MutableRule mr = findRuleByNameInStyle(rule, mutableStyle);
            if (mr != null) {
                return DefaultGlyphService.create(mr, dimension, maplayer);
            }
        }
        // Otherwise, if there is a scale, we can filter rules.
        if (scale != null) {
            final MutableRule mr = findRuleByScaleInStyle(scale, mutableStyle);
            if (mr != null) {
                return DefaultGlyphService.create(mr, dimension, maplayer);
            }
        }
        return DefaultGlyphService.create(mutableStyle, dimension, maplayer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredLegendSize(final LegendTemplate template, final MutableStyle ms) throws PortrayalException {
        final MapItem ml = getMapLayer(ms, null);
        return DefaultLegendService.legendPreferredSize(template, ml);
    }

    /**
     * Returns the {@linkplain MutableRule rule} which matches with the given name, or {@code null}
     * if none.
     *
     * @param ruleName The rule name to try finding in the given style.
     * @param ms The style for which we want to extract the rule.
     * @return The rule with the given name, or {@code null} if no one matches.
     */
    private MutableRule findRuleByNameInStyle(final String ruleName, final MutableStyle ms) {
        if (ruleName == null) {
            return null;
        }
        for (final MutableFeatureTypeStyle mfts : ms.featureTypeStyles()) {
            for (final MutableRule mutableRule : mfts.rules()) {
                if (ruleName.equals(mutableRule.getName())) {
                    return mutableRule;
                }
            }
        }
        return null;
    }

    /**
     * Returns the {@linkplain MutableRule rule} which can be applied for the given scale, or
     * {@code null} if no rules can be used at this scale.
     *
     * @param scale The scale.
     * @param ms The style for which we want to extract a rule for the given scale.
     * @return The first rule for the given scale that can be applied, or {@code null} if no
     *         one matches.
     */
    private MutableRule findRuleByScaleInStyle(final Double scale, final MutableStyle ms) {
        if (scale == null) {
            return null;
        }
        for (final MutableFeatureTypeStyle mfts : ms.featureTypeStyles()) {
            for (final MutableRule mutableRule : mfts.rules()) {
                if (scale < mutableRule.getMaxScaleDenominator() &&
                    scale > mutableRule.getMinScaleDenominator())
                {
                    return mutableRule;
                }
            }
        }
        return null;
    }

    /**
     * Always returns {@code true}.
     */
    @Override
    public boolean isQueryable(final Query query) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException {
        try {
            return new DefaultGeographicBoundingBox(getEnvelope());
        } catch (TransformException ex) {
            throw new DataStoreException(ex);
        }
    }

}
