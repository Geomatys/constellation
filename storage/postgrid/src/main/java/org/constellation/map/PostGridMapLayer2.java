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
package org.constellation.map;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReader;
import org.geotools.coverage.processing.ColorMap;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.AbstractMapLayer;
import org.geotools.map.CoverageMapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;
import org.geotools.util.MeasurementRange;

import org.opengis.feature.type.Name;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.RasterSymbolizer;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PostGridMapLayer2 extends AbstractMapLayer implements CoverageMapLayer {
    
    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgrid");

    /**
     * The requested elevation.
     */
    private double elevation;

    /**
     * The envelope of current layer, including its CRS.
     */
    private GeneralEnvelope envelope;

    /**
     * Range for the {@linkplain ColorMap color map} to apply on the
     * {@linkplain GridCoverage2D grid coverage}.
     */
    private MeasurementRange dimRange;

    /**
     * List of available dates for a request.
     */
    private final List<Date> times;

    /**
     * Connection to a PostGRID database.
     */
    private final Database db;

    /**
     * Layer to consider.
     */
    private final Layer layer;
    
    /**
     * grid coverage table.
     */
    private final GridCoverageTable table;
    
    /**
     * postgrid reader
     */
    private final PostGridReader reader;
    
    /**
     * Builds a map layer, using a database connection and a layer.
     *
     * @param db The database connection.
     * @param layer The current layer.
     */
    public PostGridMapLayer2(final Database db, final Layer layer) {
        super(createDefaultRasterStyle());
        this.db = db;
        this.layer = layer;
        this.times = new ArrayList<Date>();
        setName(layer.getName());
        
        GridCoverageTable immutableTable = null;
        try {
            immutableTable = db.getTable(GridCoverageTable.class);
        } catch (NoSuchTableException ex) {
            LOGGER.log(Level.SEVERE, "No GridCoverageTable", ex);
        }
        this.table = new GridCoverageTable(immutableTable);
        this.table.setLayer(layer);
        this.reader = new PostGridReader(table, getBounds());
    }

    public ReferencedEnvelope getBounds() {
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        final GeographicBoundingBox bbox;
        try {
            bbox = layer.getGeographicBoundingBox();
        } catch (CatalogException ex) {
            LOGGER.warning(ex.getLocalizedMessage());
            return new ReferencedEnvelope(crs);
        }
        return new ReferencedEnvelope(bbox.getWestBoundLongitude(),
                bbox.getEastBoundLongitude(),
                bbox.getSouthBoundLatitude(),
                bbox.getNorthBoundLatitude(),
                crs);
    }

    /**
     * Returns a single time from the {@linkplain #times} list, or {@code null} if none.
     * If there is more than one time, select the last one on the basis that it is typically
     * the most recent one.
     */
    private Date getTime() {
        return times.isEmpty() ? null : times.get(times.size() - 1);
    }

    /**
     * Returns the maximum range for the color map to apply.
     */
    public MeasurementRange getDimRange() {
        return dimRange;
    }

    /**
     * Returns the coordinate reference system, or {@code null} if unknown.
     *
     * @return The current CRS for queries, or {@code null}.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return (envelope != null) ? envelope.getCoordinateReferenceSystem() : null;
    }

    private static final MutableStyle createDefaultRasterStyle() {
        final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        final RasterSymbolizer symbol =sf.createRasterSymbolizer();
        return sf.createStyle(symbol);
    }

    /**
     * Fixes the range to apply for the color map.
     */
    public void setDimRange(final MeasurementRange dimRange) {
        this.dimRange = dimRange;
    }

    /**
     * Fixes the elevation to request.
     */
    public void setElevation(final double elevation) {
        this.elevation = elevation;
    }

    /**
     * Returns a modifiable list of dates.
     */
    public List<Date> times() {
        return times;
    }

    public Name getCoverageName() {
        return new NameImpl(layer.getName());
    }

    public CoverageReader getCoverageReader() {
        table.setTimeRange(getTime(), getTime());
        table.setVerticalRange(elevation, elevation);
        return reader;
    }

    public GridCoverage2D getGridCoverage2D() {
        return null;
    }

}
