/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import org.constellation.ServiceDef;

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.style.MutableStyleFactory;

import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;


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

    String KEY_DIM_RANGE = "DIM_RANGE";

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
     * @see Layer#getAvailableTimes
     */
    SortedSet<Date> getAvailableTimes() throws CatalogException;

    /**
     * @see Layer#getAvailableElevations
     */
    SortedSet<Number> getAvailableElevations() throws CatalogException;

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
            final Double elevation, final Date time) throws CatalogException, IOException;

    /**
     * Returns a list of favorites styles associated to this layer.
     */
    List<String> getFavoriteStyles();

    /**
     * @see Layer#getGeographicBoundingBox
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * @see Layer#getLegend(Dimension)
     */
    BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template);

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
     * Create a MapLayer with the given style and parameters.
     * if style is null, the favorite style of this layer will be used.
     * 
     * @param style : can be null. reconized types are String/GraphicBuilder/MutableStyle.
     * @param params : can be null.
     */
    MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) throws PortrayalException;

    /**
     * @see Layer#getName
     */
    String getName();

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
    abstract TYPE getType();
}
