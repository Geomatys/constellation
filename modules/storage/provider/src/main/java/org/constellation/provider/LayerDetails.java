/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.apache.sis.storage.DataStoreException;
import org.constellation.ServiceDef;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.DateRange;
import org.opengis.feature.type.Name;

import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.style.Style;


/**
 * Information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public interface LayerDetails {
    /**
     * Defines the type of provider for a {@linkplain Layer layer}.
     */
    enum TYPE {
        COVERAGE,
        FEATURE;
    };

    String KEY_EXTRA_PARAMETERS = "EXTRA";

    MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    FilterFactory2 FILTER_FACTORY = (FilterFactory2)FactoryFinder.getFilterFactory(
                            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));
    RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();

    /**
     * Default legend size, if not specified in the {@code GetLegend} request.
     */
    Dimension LEGEND_SIZE = new Dimension(200, 40);

    /**
     * Returns the time range of this layer. This method is typically much faster than
     * {@link #getAvailableTimes()} when only the first date and/or the last date are
     * needed, rather than the set of all available dates.
     *
     * @return The time range of this layer, or {@code null} if this information is not available.
     * @throws DataStoreException If an error occurred while fetching the time range.
     */
    DateRange getDateRange() throws DataStoreException;

    /**
     * Returns the set of dates when a coverage is available. Note that this method may
     * be slow and should be invoked only when the set of all dates is really needed.
     * If only the first or last date is needed, consider using {@link #getDateRange()}
     * instead.
     *
     * @see Layer#getAvailableTimes()
     */
    SortedSet<Date> getAvailableTimes() throws DataStoreException;

    /**
     * @see Layer#getAvailableElevations
     */
    SortedSet<Number> getAvailableElevations() throws DataStoreException;

    /**
     * Returns the coverage requested.
     *
     * @param envelope The {@link Envelope} to request. Should  not be {@code null}.
     * @param dimension A {@link Dimension} for the image. Should  not be {@code null}.
     * @param elevation The elevation to request, in the case of nD data.
     * @param time The date for the data, in the case of temporal data.
     *
     * @throws org.constellation.catalog.CatalogException
     * @throws java.io.IOException
     */
    GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws DataStoreException, IOException;

    /**
     * @see Layer#getGeographicBoundingBox
     */
    GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException;
    
    /**
     * Returns the native envelope of this layer.
     */
    Envelope getEnvelope() throws DataStoreException;

    /**
     * Returns the legend graphic representation for the layer.
     *
     * @param dimension The dimension of the output legend graphic.
     * @param template The legend template to apply for the legend response.
     * @param style The style to apply on the output.
     * @param rule The rule to apply from the style.
     * @param scale The scale for which the rule must comply.
     * @return A legend graphic for this data.
     * @throws PortrayalException if an error occurs while trying to generate the legend graphic.
     */
    BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template,
                                   final Style style, final String rule, final Double scale)
                                   throws PortrayalException;

    /**
     * Returns the preferred size for the legend.
     *
     * @param template The legend template to apply for the legend response.
     * @param ms The layer style.
     * @return The preferred dimensions for the legend response.
     * @throws PortrayalException if an error occurs in getting the {@link MapLayer}.
     */
    Dimension getPreferredLegendSize(final LegendTemplate template, final MutableStyle ms) throws PortrayalException;

    /**
     * Create a MapItem with the given style and parameters.
     * if style is null, the favorite style of this layer will be used.
     *
     * @param style : can be null. reconized types are String/GraphicBuilder/MutableStyle.
     * @param params : can be null.
     */
    MapItem getMapLayer(MutableStyle style, final Map<String, Object> params) throws PortrayalException;

    /**
     * @see Layer#getName
     */
    Name getName();

    /**
     * @see Layer#getSampleValueRanges
     */
    MeasurementRange<?>[] getSampleValueRanges();

    /**
     * Returns {@code true} if the layer is queryable by the specified service.
     *
     * @see Layer#isQueryable
     */
    boolean isQueryable(ServiceDef.Query query);

    /**
     * Returns the type of provider for a {@linkplain Layer layer}.
     */
    TYPE getType();

    /**
     * Origin source of this data can be :
     * FeatureCollection, CoverageRefence, null.
     */
    Object getOrigin();

}
