/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.coverage.catalog;

import java.util.Set;
import java.util.Date;
import java.util.SortedSet;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.DateRange;

import org.constellation.catalog.Element;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.model.Model;
import org.constellation.ws.ServiceType;


/**
 * A layer of {@linkplain Coverage coverages} sharing common properties.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Layer extends Element {
    /**
     * Returns the thematic for this layer. Examples: <cite>temperature</cite>,
     * <cite>sea level anomaly</cite>, <cite>chlorophylle-a concentration</cite>, etc.
     *
     * @return The thematic for this layer, or {@code null} if unknown.
     */
    String getThematic();

    /**
     * Returns the procedure applied on this layer. Examples: <cite>gradient magnitude</cite>,
     * <cite>monthly average</cite>, etc.
     *
     * @return The procedure for this layer, or {@code null} if unknown.
     */
    String getProcedure();

    /**
     * A layer to use as a fallback if no data is available in this layer for a given position. For
     * example if no data is available in a weekly averaged <cite>Sea Surface Temperature</cite>
     * (SST) coverage because a location is masked by clouds, we may want to look in the mounthly
     * averaged SST coverage as a fallback.
     *
     * @return The fallback layer, or {@code null} if none.
     */
    Layer getFallback();

    /**
     * Returns all series for this layer.
     *
     * @return The series, or an empty set if none.
     */
    Set<Series> getSeries();

    /**
     * Returns the series for the given name, or {@code null} if none. Names are case-sensitive,
     * because the series name may be a short variable name with different meaning for <var>t</var>
     * and <var>T</var>. Leading and trealing spaces are ignored in order to avoid unexpected
     * mismatch between columns using the {@code character} and the {@code character varying}
     * SQL types.
     *
     * @param  name The case-sensitive series name.
     * @return The series in this layer for the given name, or {@code null} if none.
     */
    Series getSeries(String name);

    /**
     * Returns a typical time intervale (in days) between two coverages of this layer. For example
     * a layer of weekly <cite>Sea Surface Temperature</cite> (SST) coverages may returns 7, while
     * a layer of mounthly SST coverage may returns 30. This value is only approximative.
     *
     * @return A typical, approximative time interval between coverages in this layer,
     *         or {@link Double#NaN} if unknown or not applicable.
     */
    double getTimeInterval();

    /**
     * Returns the set of dates when a coverage is available.
     *
     * @return The set of dates.
     * @throws CatalogException if an error occured while fetching the set.
     */
    SortedSet<Date> getAvailableTimes() throws CatalogException;

    /**
     * Returns the set of altitudes where a coverage is available. If different coverages
     * have different set of altitudes, then this method returns only the altitudes that
     * are common to every coverages.
     *
     * @return The set of altitudes. May be empty, but will never be null.
     * @throws CatalogException if an error occured while fetching the set.
     */
    SortedSet<Number> getAvailableElevations() throws CatalogException;

    /**
     * Returns the ranges of valid sample values for each band.
     * The ranges are always expressed in <cite>geophysics</cite> values.
     *
     * @return The range of valis sample values.
     */
    MeasurementRange<?>[] getSampleValueRanges();

    /**
     * Returns a time range encompassing all coverages in this layer.
     *
     * @return The time range encompassing all coverages.
     * @throws CatalogException if an error occured while fetching the time range.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Returns a geographic bounding box encompassing all coverages in this layer.
     *
     * @return The geographic bounding box encompassing all coverages.
     * @throws CatalogException if an error occured while fetching the bounding box.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * Returns the typical pixel resolution in this layer, or {@code null} if unknown.
     * Values are degrees of longitude and latitude.
     *
     * @return The typical pixel resolution.
     * @throws CatalogException if an error occured while fetching the resolution.
     */
    Dimension2D getTypicalResolution() throws CatalogException;

    /**
     * Returns a typical image bounds in this layer, or {@code null} if unknown.
     * This is a hint only - there is no garantee that images will really have this bounds.
     *
     * @return The typical image bounds, or {@code null} if unknown.
     * @throws CatalogException if an error occured while fetching the bounds.
     */
    Rectangle getTypicalBounds() throws CatalogException;

    /**
     * Returns a reference to a coverage for the given date and elevation.
     *
     * @param  time The date, or {@code null} if not applicable.
     * @param  elevation The elevation, or {@code null} if not applicable.
     * @return A reference to a coverage.
     * @throws CatalogException if an error occured while querying the catalog.
     */
    CoverageReference getCoverageReference(Date time, Number elevation) throws CatalogException;

    /**
     * Returns a reference to every coverages available in this layer. Note that the set of
     * coverages is rectricted by the layer {@linkplain #getTimeRange time range} and
     * {@linkplain #getGeographicBoundingBox geographic bounding box}.
     * <p>
     * Note that coverages are not immediately loaded; only references are returned.
     *
     * @return The set of coverages in the layer.
     * @throws CatalogException if an error occured while querying the catalog.
     */
    Set<CoverageReference> getCoverageReferences() throws CatalogException;

    /**
     * Returns a reference to a single "typical" coverages available in this layer.
     * This method is especially useful for layer that are expected to contains only
     * one coverage.
     * <p>
     * Note that the coverages is not immediately loaded; only a reference is returned.
     *
     * @return A typical coverage in the layer, or {@code null} if none.
     * @throws CatalogException if an error occured while querying the catalog.
     */
    CoverageReference getCoverageReference() throws CatalogException;

    /**
     * Returns a view of this layer as a coverage. This coverage can be evaluated at
     * (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>) location, using interpolations
     * if needed. Note that this coverage is less elaborated than {@link Descriptor#getCoverage}:
     * <p>
     * <ul>
     *   <li>No {@linkplain Operation operation} or {@link RegionOfInterest region of interest}
     *       are applied.</li>
     *   <li>Values are evaluated on this layer only, never on the {@linkplain #getFallback fallback}.</li>
     *   <li>This method may send a whole images through the network, instead of performing the
     *       evaluations on a remote server.</li>
     * </ul>
     *
     * @return A view of this layer as a <var>n</var>-dimensional coverage.
     * @throws CatalogException if the coverage can not be created.
     */
    Coverage getCoverage() throws CatalogException;

    /**
     * Returns a legend of the specified dimension. The legend is a
     * {@linkplain org.geotoolkit.gui.swing.image.ColorRamp color ramp}
     * with gratuation on it.
     *
     * @param  dimension The dimension of the image to be returned.
     * @return The color ramp as an image of the specified dimension.
     */
    BufferedImage getLegend(Dimension dimension);

    /**
     * If this layer is the result of a numerical model, returns this model.
     * Otherwise returns {@code null}.
     *
     * @return The numerical model, or {@code null} if none.
     * @throws CatalogException if an error occured while querying the catalog.
     */
    Model getModel() throws CatalogException;

    /**
     * Return {@code true} if all series are queryable by the specified service.
     * This information is inferred from the {@code Permissions} table.
     *
     * @param service The web service trying to request this layer (WMS or WCS).
     * @return {@code true} if all series are queryable for the given service.
     */
    boolean isQueryable(ServiceType service);
}
