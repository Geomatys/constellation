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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.web.Service;
import org.constellation.query.wms.GetFeatureInfo;

import org.geotools.display.exception.PortrayalException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.map.MapLayer;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleFactory;
import org.geotools.util.MeasurementRange;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public interface LayerDetails {
    
    static final StyleFactory STYLE_FACTORY = CommonFactoryFinder.getStyleFactory(null);
    static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();
    
    /**
     * Default legend size, if not specified in the {@code GetLegend} request.
     */
    public static final Dimension LEGEND_SIZE = new Dimension(200, 40);

    /**
     * @see Layer#getAvailableTimes
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException;

    /**
     * @see Layer#getAvailableElevations
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException;

    /**
     * Returns a list of favorites styles associated to this layer.
     */
    public List<String> getFavoriteStyles();

    /**
     * @see Layer#getGeographicBoundingBox
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * Returns the information at the coordinates specified, if the layer
     * {@linkplain Layer#isQueryable is queryable}. 
     *
     * @return The information at the coordinates specified.
     * @throws CatalogException
     * @throws IOException
     */
    public Object getInformationAt(final GetFeatureInfo gfi) throws CatalogException, IOException;

    /**
     * @see Layer#getLegend(Dimension)
     */
    public BufferedImage getLegendGraphic(final Dimension dimension);

    /**
     * Create a MapLayer with the given style and parameters.
     * if style is null, the favorite style of this layer will be used.
     * 
     * @param style : can be null. reconized types are String/GraphicBuilder/MutableStyle.
     * @param params : can be null.
     */
    public MapLayer getMapLayer(Object style, final Map<String, Object> params) throws PortrayalException;

    /**
     * @see Layer#getName
     */
    public String getName();

    /**
     * Returns the coordinates of the requested pixel in the image, expressed in the
     * {@linkplain CoordinateReferenceSystem crs} defined in the request.
     */
    public GeneralDirectPosition getPixelCoordinates(final GetFeatureInfo gfi);

    /**
     * @see Layer#getRemarks
     */
    public String getRemarks();

    /**
     * @see Layer#getSampleValueRanges
     */
    public MeasurementRange<?>[] getSampleValueRanges();

    /**
     * @see Layer#getThematic
     */
    public String getThematic();

    /**
     * Returns {@code true} if the layer is queryable by the specified service.
     *
     * @see Layer#isQueryable 
     */
    public boolean isQueryable(Service service);
}
