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
package org.constellation.coverage.catalog;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.imageio.IIOException;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.geotoolkit.image.io.mosaic.Tile;
import org.geotoolkit.image.io.mosaic.TileManager;
import org.geotoolkit.image.io.mosaic.TileManagerFactory;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.util.collection.SoftValueHashMap;
import org.geotoolkit.util.XArrays;
import org.geotoolkit.resources.Errors;

import org.constellation.catalog.Table;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain Tiles tiles}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class TileTable extends Table {
    /**
     * Shared instance of a table of grid geometries. Will be created only when first needed.
     */
    private transient GridGeometryTable gridGeometryTable;

    /**
     * A cache of tile managers created up to date.
     */
    private final Map<Request,TileManager[]> cache;

    /**
     * Creates a tile table.
     *
     * @param database Connection to the database.
     */
    public TileTable(final Database database) {
        super(new TileQuery(database));
        cache = new SoftValueHashMap<Request,TileManager[]>();
    }

    /**
     * Returns the tile manager for the given layer and date range. This method usually returns a
     * single tile manager, but more could be returned if the tiles can not fit all in the same
     * instance.
     *
     * @param  layer     The layer.
     * @param  startTime The start time, or {@code null} if none.
     * @param  endTime   The end time, or {@code null} if none.
     * @param  srid      The numeric identifier of the CRS.
     * @return The tile managers for the given series and date range.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized TileManager[] getTiles(final Layer layer,
            final Timestamp startTime, final Timestamp endTime, final int srid)
            throws CatalogException, SQLException, IOException
    {
        final Request request = new Request(layer, startTime, endTime, srid);
        TileManager[] managers = cache.get(request);
        if (managers != null) {
            return managers;
        }
        final TileQuery query = (TileQuery) this.query;
        final Calendar calendar = getCalendar();
        final PreparedStatement statement = getStatement(QueryType.LIST);
        statement.setString   (indexOf(query.byLayer), layer.getName());
        statement.setTimestamp(indexOf(query.byStartTime), startTime, calendar);
        statement.setTimestamp(indexOf(query.byEndTime),   endTime,   calendar);
        statement.setInt      (indexOf(query.byHorizontalSRID), srid);
        final int seriesIndex   = indexOf(query.series);
        final int filenameIndex = indexOf(query.filename);
        final int indexIndex    = indexOf(query.index);
        final int extentIndex   = indexOf(query.spatialExtent);
        final int dxIndex       = indexOf(query.dx);
        final int dyIndex       = indexOf(query.dy);
        final List<Tile> tiles  = new ArrayList<Tile>();
        final ResultSet results = statement.executeQuery();
        Series            series       = null;
        ImageReaderSpi    provider     = null;
        GridGeometryEntry geometry     = null;
        String            lastSeriesID = null;
        String            lastExtentID = null;
        while (results.next()) {
            final String seriesID = results.getString(seriesIndex);
            final String filename = results.getString(filenameIndex);
            final int       index = results.getInt   (indexIndex);
            final String   extent = results.getString(extentIndex);
            final int          dx = results.getInt   (dxIndex); // '0' if null, which is fine.
            final int          dy = results.getInt   (dyIndex); // '0' if null, which is fine.
            /*
             * Gets the series, which usually never change for the whole mosaic (but this is not
             * mandatory - the real thing that can't change is the layer).  The series is needed
             * in order to build the absolute pathname from the relative one.
             */
            if (!seriesID.equals(lastSeriesID)) {
                // Computes only if the series changed. Usually it doesn't change.
                series       = layer.getSeries(seriesID);
                provider     = getImageReaderSpi(series.getFormat().getImageFormat());
                lastSeriesID = seriesID;
            }
            Object input = series.file(filename);
            if (!((File) input).isAbsolute()) try {
                input = series.uri(filename);
            } catch (URISyntaxException e) {
                throw new IIOException(e.getLocalizedMessage(), e);
            }
            /*
             * Gets the geometry, which usually don't change often.  The same geometry can be shared
             * by all tiles at the same level, given that the only change is the (dx,dy) translation
             * term defined explicitly in the "Tiles" table. Doing so avoid the creation a thousands
             * of new "GridGeometries" entries.
             */
            if (!extent.equals(lastExtentID)) {
                if (gridGeometryTable == null) {
                    gridGeometryTable = getDatabase().getTable(GridGeometryTable.class);
                }
                geometry = gridGeometryTable.getEntry(extent);
                lastExtentID = extent;
            }
            AffineTransform gridToCRS = geometry.gridToCRS;
            if (dx != 0 || dy != 0) {
                gridToCRS = new AffineTransform(gridToCRS);
                gridToCRS.translate(dx, dy);
            }
            final Rectangle bounds = geometry.getBounds();
            final Tile tile = new Tile(provider, input, (index != 0) ? index-1 : 0, bounds, gridToCRS);
            tiles.add(tile);
        }
        results.close();
        if (!tiles.isEmpty()) {
            managers = TileManagerFactory.DEFAULT.create(tiles);
            cache.put(request, managers);
        }
        return managers;
    }

    /**
     * Returns an image reader for the specified name. The argument can be either a format
     * name or a mime type.
     *
     * @todo Move this as a public method in {@link Format} interface.
     */
    private static ImageReaderSpi getImageReaderSpi(final String format) throws IIOException {
        final IIORegistry registry = IIORegistry.getDefaultInstance();
        Iterator<ImageReaderSpi> providers = registry.getServiceProviders(ImageReaderSpi.class, true);
        while (providers.hasNext()) {
            final ImageReaderSpi provider = providers.next();
            if (XArrays.contains(provider.getFormatNames(), format)) {
                return provider;
            }
        }
        /*
         * Tests for MIME type only if no provider was found for the format name. We do not merge
         * the check for MIME type in the above loop because it has a cost (getMIMETypes() clones
         * an array) and should not be needed for database registering their format by name. This
         * check is performed mostly for compatibility purpose with policy in previous versions.
         */
        providers = registry.getServiceProviders(ImageReaderSpi.class, true);
        while (providers.hasNext()) {
            final ImageReaderSpi provider = providers.next();
            if (XArrays.contains(provider.getMIMETypes(), format)) {
                return provider;
            }
        }
        throw new IIOException(Errors.format(Errors.Keys.NO_IMAGE_READER));
    }

    /**
     * A request submitted to {@link TileTable}.
     *
     * @todo We probably need to refactor this approach in a more generic way so that other
     *       table can cache their requests in the same way.
     */
    private static final class Request {
        private final String layer;
        private final long startTime;
        private final long endTime;
        private final int srid;

        public Request(final Layer layer, final Timestamp startTime, final Timestamp endTime, final int srid) {
            this.layer     = layer.getName();
            this.startTime = (startTime != null) ? startTime.getTime() : Long.MIN_VALUE;
            this.endTime   =   (endTime != null) ?   endTime.getTime() : Long.MAX_VALUE;
            this.srid      = srid;
        }

        @Override
        public int hashCode() {
            return layer.hashCode() + srid;
        }

        @Override
        public boolean equals(final Object object) {
            if (object instanceof Request) {
                final Request that = (Request) object;
                return Utilities.equals(this.layer, that.layer) &&
                       this.startTime == that.startTime &&
                       this.endTime   == that.endTime &&
                       this.srid      == that.srid;
            }
            return false;
        }

        @Override
        public String toString() {
            return layer;
        }
    }
}
