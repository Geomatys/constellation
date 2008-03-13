/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
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

import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileManager;
import org.geotools.image.io.mosaic.TileManagerFactory;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.seagis.catalog.Table;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain Tiles tiles}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class TileTable extends Table {
    /**
     * The table of series. A shared instance will be fetched when first needed.
     */
    private SeriesTable seriesTable;

    /**
     * Shared instance of a table of grid geometries. Will be created only when first needed.
     */
    private transient GridGeometryTable gridGeometryTable;

    /**
     * Creates a tile table.
     *
     * @param database Connection to the database.
     */
    public TileTable(final Database database) {
        super(new TileQuery(database));
    }

    /**
     * Returns the tile manager for the given layer and date range. This method usually returns a
     * single tile manager, but more could be returned if the tiles can not fit all in the same
     * instance.
     *
     * @param  layer The layer name.
     * @return The tile managers for the given series and date range.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized TileManager[] getTiles(final String layer, final Date startTime, final Date endTime, final int srid)
            throws CatalogException, SQLException, IOException
    {
        final TileQuery query = (TileQuery) this.query;
        final Calendar calendar = getCalendar();
        final PreparedStatement statement = getStatement(QueryType.LIST);
        statement.setString   (indexOf(query.byLayer), layer);
        statement.setTimestamp(indexOf(query.byStartTime), new Timestamp(startTime.getTime()), calendar);
        statement.setTimestamp(indexOf(query.byEndTime),   new Timestamp(  endTime.getTime()), calendar);
        statement.setInt      (indexOf(query.byHorizontalSRID), srid);
        statement.setBoolean  (indexOf(query.byVisibility), true);
        final int seriesIndex   = indexOf(query.series);
        final int filenameIndex = indexOf(query.filename);
        final int indexIndex    = indexOf(query.index);
        final int extentIndex   = indexOf(query.spatialExtent);
        final List<Tile> tiles  = new ArrayList<Tile>();
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String seriesName = results.getString(seriesIndex);
            final String   filename = results.getString(filenameIndex);
            final int         index = results.getInt   (indexIndex);
            final String     extent = results.getString(extentIndex);
            if (seriesTable == null) {
                seriesTable = getDatabase().getTable(SeriesTable.class);
            }
            final Series series = seriesTable.getEntry(seriesName);
            Object input = series.file(filename);
            if (!((File) input).isAbsolute()) try {
                input = series.uri(filename);
            } catch (URISyntaxException e) {
                throw new IIOException(e.getLocalizedMessage(), e);
            }
            final ImageReaderSpi spi = getImageReaderSpi(series.getFormat().getImageFormat());
            if (gridGeometryTable == null) {
                gridGeometryTable = getDatabase().getTable(GridGeometryTable.class);
            }
            final GridGeometryEntry geometry = gridGeometryTable.getEntry(extent);
            final Rectangle bounds = geometry.getBounds();
            final AffineTransform gridToCRS = geometry.getGridToCRS2D();
            final Tile tile = new Tile(spi, input, index, bounds, gridToCRS);
            tiles.add(tile);
        }
        results.close();
        return TileManagerFactory.DEFAULT.create(tiles);
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
            if (XArray.contains(provider.getFormatNames(), format)) {
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
            if (XArray.contains(provider.getMIMETypes(), format)) {
                return provider;
            }
        }
        throw new IIOException(Errors.format(ErrorKeys.NO_IMAGE_READER));
    }
}
