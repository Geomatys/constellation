/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.web.Service;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;
import org.geotools.util.MeasurementRange;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public interface LayerDetails {
    /**
     * Default legend size, if not specified in the {@code GetLegend} request.
     */
    public static final Dimension LEGEND_SIZE = new Dimension(200, 40);

    public MapLayer getMapLayer(final Map<String, Object> params);

    public MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params);

    /**
     * @see Layer#getLegend(Dimension)
     */
    public BufferedImage getLegendGraphic(final Dimension dimension);

    /**
     * @see Layer#getName
     */
    public String getName();

    /**
     * @see Layer#isQueryable
     */
    public boolean isQueryable(Service service);

    /**
     * @see Layer#getGeographicBoundingBox
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * @see Layer#getAvailableTimes
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException;

    /**
     * @see Layer#getAvailableElevations
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException;

    /**
     * @see Layer#getSampleValueRanges
     */
    public MeasurementRange<?>[] getSampleValueRanges();

    /**
     * @see Layer#getRemarks
     */
    public String getRemarks();

    /**
     * @see Layer#getThematic
     */
    public String getThematic();
}
