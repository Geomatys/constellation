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
package org.constellation.map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.processing.ColorMap;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.map.AbstractMapLayer;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.feature.DefaultName;

import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.RasterSymbolizer;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PostGridMapLayer extends AbstractMapLayer implements CoverageMapLayer {

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
     * postgrid reader
     */
    private final PostGridReader reader;
    
    /**
     * Builds a map layer, using a database connection and a layer.
     *
     * @param db The database connection.
     * @param layer The current layer.
     */
    public PostGridMapLayer(final PostGridReader reader) {
        super(createDefaultRasterStyle());
        this.reader = reader;
        setName(reader.getTable().getLayer().getName());
    }

    @Override
    public Envelope getBounds() {
        //        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        //        final GeographicBoundingBox bbox;
        //        try {
        //            bbox = reader.getTable().getLayer().getGeographicBoundingBox();
        //        } catch (CatalogException ex) {
        //            LOGGER.warning(ex.getLocalizedMessage());
        //            return new ReferencedEnvelope(crs);
        //        }
        //        return new ReferencedEnvelope(bbox.getWestBoundLongitude(),
        //                bbox.getEastBoundLongitude(),
        //                bbox.getSouthBoundLatitude(),
        //                crs);
        //                crs);

        try {
            return reader.getGridGeometry(0).getEnvelope();
        } catch (CoverageStoreException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
            //todo what should we do ?
            return null;
        }
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
        final MutableStyleFactory sf = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
        final RasterSymbolizer symbol =sf.rasterSymbolizer();
        return sf.style(symbol);
    }

    /**
     * Fixes the range to apply for the color map.
     */
    public void setDimRange(final MeasurementRange dimRange) {
        this.dimRange = dimRange;
    }

    @Override
    public Name getCoverageName() {
        return new DefaultName(reader.getTable().getLayer().getName());
    }

    @Override
    public GridCoverageReader getCoverageReader() {
        return reader;
    }


}
