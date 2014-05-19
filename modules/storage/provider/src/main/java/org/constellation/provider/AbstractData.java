/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.provider;

import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Query;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.ext.legend.DefaultLegendService;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.DateRange;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;

import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.Style;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstract layer, handle name and styles.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractData implements Data{

    protected static final Logger LOGGER = Logging.getLogger(AbstractData.class);

    /**
     * Favorites styles associated with this layer.
     */
    @Deprecated
    protected final List<String> favorites;

    /**
     * Layer name
     */
    protected final Name name;

    public AbstractData(Name name, List<String> favorites){
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
    public BufferedImage getLegendGraphic(Dimension dimension, final LegendTemplate template,
                                          final Style style, final String rule, final Double scale)
                                          throws PortrayalException
    {


        MutableStyle mutableStyle = null;
        if (style != null) {
            mutableStyle = (MutableStyle) style;
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
            final Envelope env = getEnvelope();
            if (env != null) {
                final DefaultGeographicBoundingBox result = new DefaultGeographicBoundingBox();
                result.setBounds(env);
                return result;
            } else {
                LOGGER.warning("Null boundingBox for Layer:" + name + ". Returning World BBOX.");
                return new DefaultGeographicBoundingBox(-180, 180, -90, 90);
            }
        } catch (TransformException ex) {
            throw new DataStoreException(ex);
        }
    }

    protected Filter buildCQLFilter(final String cql, final Filter filter) {
        final FilterFactory2 factory = (FilterFactory2) FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));
        try {
            final Filter cqlfilter = CQL.parseFilter(cql);
            if (filter != null) {
                return factory.and(cqlfilter, filter);
            } else {
                return cqlfilter;
            }
        } catch (CQLException ex) {
            LOGGER.log(Level.INFO,  ex.getMessage(),ex);
        }
        return filter;
    }

    protected Filter buildDimFilter(final String dimName, final String dimValue, final Filter filter) {
        final FilterFactory2 factory = (FilterFactory2) FactoryFinder.getFilterFactory(new Hints(Hints.FILTER_FACTORY,FilterFactory2.class));
        Object value = dimValue;
        try {
            value = Double.parseDouble(dimValue);
        } catch (NumberFormatException ex) {
            // not a number
        }
        final Filter extraDimFilter = factory.equals(factory.property(dimName), factory.literal(value));
        if (filter != null) {
            return factory.and(extraDimFilter, filter);
        } else {
            return extraDimFilter;
        }
    }
}
