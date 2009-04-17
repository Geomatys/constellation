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

import java.awt.Point;
import java.sql.Types;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.net.URL;
import java.net.URI;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.imageio.ImageReader;
import java.lang.reflect.UndeclaredThrowableException;

import org.geotoolkit.util.DateRange;
import org.geotoolkit.image.io.mosaic.Tile;

import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.UpdatePolicy;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;
import org.geotoolkit.resources.Errors;


/**
 * A grid coverage table with write capabilities. This class can be used in order to insert new
 * image in the database. Note that adding new records in the {@code "GridCoverages"} table may
 * imply adding new records in dependent tables like {@code "GridGeometries"}. This class may
 * add new records, but will never modify existing records.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class WritableGridCoverageTable extends GridCoverageTable {
    /**
     * {@code true} if the default extent identifier (when new one need to be created) should
     * contains the filename. A value of {@code false} create simplier extent name for series
     * that are expected to contains coverage having uniform geographic extent. But a value
     * of {@code true} is safer if the series may contains heterogeneous coverage or mosaics,
     * since it reduce the risk that {@link GridGeometryTable#searchFreeIdentifier} run out
     * of identifiers.
     */
    private static final boolean DEFAULT_EXTENT_CONTAINS_FILENAME = true;

    /**
     * The series to be returned by {@link #getSeries}, or {@code null} if unknown.
     * In the later case, the series will be inferred from the layer.
     */
    private Series series;

    /**
     * {@code true} if this table is allowed to insert new {@link Layer} rows.
     * The default value is {@code false}.
     */
    private boolean canInsertNewLayers = false;

    /**
     * The object to use for writting in the {@code "Tiles"} table.
     */
    private transient WritableGridCoverageTable tilesTable;

    /**
     * Constructs a new {@code WritableGridCoverageTable}.
     *
     * @param database The connection to the database.
     */
    public WritableGridCoverageTable(final Database database) {
        super(database);
    }

    /**
     * Constructs a new {@code WritableGridCoverageTable} from the specified query.
     */
    private WritableGridCoverageTable(final GridCoverageQuery query) {
        super(query);
    }

    /**
     * Constructs a new {@code WritableGridCoverageTable} with the same initial configuration
     * than the specified table.
     *
     * @param table The table to use as a template.
     */
    public WritableGridCoverageTable(final WritableGridCoverageTable table) {
        super(table);
        canInsertNewLayers = table.canInsertNewLayers;
    }

    /**
     * Returns {@code true} if this table is allowed to insert new {@link Layer} rows.
     * The default value is {@code false}.
     *
     * @return {@code true} if this table is allowed to insert new layers.
     */
    public boolean canInsertNewLayers() {
        return canInsertNewLayers;
    }

    /**
     * Specifies whatever this table is allowed to insert new {@link Layer} rows.
     * The default value is {@code false}.
     *
     * @param allowed {@code true} for allowing this table to insert new layers.
     */
    public void setCanInsertNewLayers(final boolean allowed) {
        canInsertNewLayers = allowed;
    }

    /**
     * Sets the layer as a string. If no layer exists for the given name and
     * {@link #canInsertNewLayers} returns {@code true}, a new one will be created.
     *
     * @param  name The layer name.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If the database access failed for an other reason.
     */
    @Override
    public synchronized void setLayer(String name) throws CatalogException, SQLException {
        if (canInsertNewLayers) {
            final LayerTable layers = getDatabase().getTable(LayerTable.class);
            name = layers.getIdentifier(name);
            setLayer(layers.getEntry(name));
        } else {
            super.setLayer(name);
        }
    }

    /**
     * Sets the series in which to insert the entries. It should be an existing series in the
     * currently selected layer.
     *
     * @param  name The series name.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If the database access failed for an other reason.
     */
    public synchronized void setSeries(final String name) throws CatalogException, SQLException {
        final Layer layer = getLayer();
        if (layer == null) {
            series = getDatabase().getTable(SeriesTable.class).getEntry(name);
            // Do not invokes setLayer since it still null for SeriesEntry created that way.
        } else {
            series = layer.getSeries(name);
            if (series == null) {
                throw new NoSuchRecordException(Errors.format(Errors.Keys.ILLEGAL_ARGUMENT_$2, "name", name));
            }
        }
    }

    /**
     * Returns the currently selected series. If {@link #setSeries} has been invoked,
     * the given series is returned. Otherwise if the current layer contains exactly one
     * series, than this series is returned since there is no ambiguity. Otherwise an
     * exception is thrown.
     *
     * @return The series for the {@linkplain #getLayer current layer}.
     * @throws CatalogException if no series can be inferred from the current layer.
     */
    @Override
    public synchronized Series getSeries() throws CatalogException {
        return (series != null) ? series : super.getSeries();
    }

    /**
     * Add an entry inferred from the specified image reader. The {@linkplain ImageReader#getInput
     * reader input} must be set, and its {@linkplain ImageReader#getImageMetadata metadata} shall
     * conforms to the Geotools {@linkplain GeographicMetadata geographic metadata}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param  reader The image reader.
     * @return The number of images inserted (should be 0 or 1).
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public int addEntry(final ImageReader reader) throws CatalogException, SQLException, IOException {
        return addEntries(Collections.singleton(reader), 0);
    }

    /**
     * Add an entry inferred from the specified tile. Note that even if the argument type is a
     * {@linkplain Tile}, the entry will be added in the {@code "GridCoverages"} table, not in
     * the {@code "Tiles"} table. For the later case, use {@link #addTiles} instead.
     *
     * @param  tile The tile to add.
     * @return The number of images inserted (should be 0 or 1).
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public int addEntry(final Tile tile) throws CatalogException, SQLException, IOException {
        return addEntries(Collections.singleton(tile), tile.getImageIndex());
    }

    /**
     * Adds entries inferred from the specified image inputs. The default implementation delegates
     * its work to {@link #addEntries(Iterator,int)}.
     *
     * @param  inputs The image inputs.
     * @param  imageIndex The index of the image to insert in the database.
     * @return The number of images inserted.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public int addEntries(final Collection<?> inputs, final int imageIndex)
            throws CatalogException, SQLException, IOException
    {
        return addEntries(inputs.iterator(), imageIndex);
    }

    /**
     * Adds entries inferred from the specified image inputs. The inputs shall be
     * {@link ImageReader} instances with {@linkplain ImageReader#getInput input} set
     * and {@linkplain ImageReader#getImageMetadata metadata} conform to the Geotools
     * {@linkplain GeographicMetadata geographic metadata}.
     *
     * {@link org.geotoolkit.image.io.mosaic.Tile} input are also accepted, and in some case
     * {@link File}, {@link URL} or {@link String}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param  inputs The image inputs. The iterator may recycle the same reader with different
     *                {@linkplain ImageReader#getInput input} on each call to {@link Iterator#next}.
     * @param  imageIndex The index of the image to insert in the database.
     * @return The number of images inserted.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public synchronized int addEntries(final Iterator<?> inputs, final int imageIndex)
            throws CatalogException, SQLException, IOException
    {
        int count = 0;
        if (inputs.hasNext()) {
            final Object next = inputs.next();
            boolean success = false;
            final WritableGridCoverageIterator iterator =
                    new WritableGridCoverageIterator(this, null, imageIndex, inputs, next);
            final Series oldSeries = series;
            transactionBegin();
            try {
                count = addEntriesUnsafe(iterator);
                success = true; // Must be the very last line in the try block.
            } finally {
                series = oldSeries;
                transactionEnd(success);
            }
        }
        return count;
    }

    /**
     * Adds entries without the protection provided by the database rollback mechanism.
     * The commit or rollback must be performed by the caller.
     *
     * @return The number of images inserted.
     */
    private int addEntriesUnsafe(final Iterator<WritableGridCoverageEntry> entries)
            throws CatalogException, SQLException, IOException
    {
        assert Thread.holdsLock(this);
        int count = 0;
        final Series            oldSeries = series;
        final GridCoverageQuery query     = (GridCoverageQuery) this.query;
        final Calendar          calendar  = getCalendar();
        final PreparedStatement statement = getStatement(QueryType.INSERT);
        final GridGeometryTable gridTable = getDatabase().getTable(GridGeometryTable.class);
        final int bySeries    = indexOf(query.series);
        final int byFilename  = indexOf(query.filename);
        final int byIndex     = indexOf(query.index);
        final int byStartTime = indexOf(query.startTime);
        final int byEndTime   = indexOf(query.endTime);
        final int byExtent    = indexOf(query.spatialExtent);
        final int byDx = (query.dx != null) ? query.dx.indexOf(QueryType.INSERT) : 0;
        final int byDy = (query.dy != null) ? query.dy.indexOf(QueryType.INSERT) : 0;
        final boolean explicitTranslate = (byDx != 0 && byDy != 0);
        while (entries.hasNext()) {
            final WritableGridCoverageEntry entry;
            try {
                entry = entries.next();
            } catch (UndeclaredThrowableException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                if (cause instanceof SQLException) {
                    throw (SQLException) cause;
                }
                if (cause instanceof CatalogException) {
                    throw (CatalogException) cause;
                }
                throw exception;
            }
            /*
             * If we are scanning new files for a specific series, gets that series.
             * Otherwise try to guess it from the path name and file extension.
             */
            series = oldSeries;
            if (entry.series != null) {
                series = entry.series;
            } else if (series == null) {
                final Layer layer = getNonNullLayer();
                final Set<Series> candidates = layer.getSeries();
                if (!candidates.isEmpty()) {
                    series = entry.choose(candidates);
                } else {
                    final SeriesTable table = getDatabase().getTable(SeriesTable.class);
                    final String path = (entry.path != null) ? entry.path.getPath() : "";
                    final String ID = table.getIdentifier(layer.getName(), path,
                                    entry.extension, entry.getFormatName(true));
                    series = table.getEntry(ID);
                }
            }
            /*
             * Logs a message about the entry to be created, then suggests a name for the extent
             * as the concatenation of series name and the file name.  This is not yet the final
             * name, since it may change as a result of getIdentier(...) call later.
             *
             * TODO: localize the log.
             */
            String extent = series.getName();
            final LogRecord record = new LogRecord(Level.INFO, "Adding \"" +
                    entry.filename + "\" to series \"" + extent + "\".");
            record.setSourceClassName(WritableGridCoverageTable.class.getSimpleName());
            record.setSourceMethodName("addEntries");
            LOGGER.log(record);
            if (DEFAULT_EXTENT_CONTAINS_FILENAME) {
                final String name = entry.filename.replace('_','-').replace(' ','_').trim();
                if (!extent.equalsIgnoreCase(name)) {
                    extent = extent + ' ' + name;
                }
            }
            /*
             * Gets the metadata of interest. The metadata should contains at least the image
             * envelope and CRS. If it doesn't, then we will use the table envelope as a fall
             * back. It defaults to the whole Earth in WGS 84 geographic coordinates, but the
             * user can set an other value using {@link #setEnvelope}.
             */
            entry.parseMetadata(getDatabase());
            extent = gridTable.getIdentifier(
                    entry.getImageSize(),
                    entry.getGridToCRS(!explicitTranslate),
                    entry.getHorizontalSRID(),
                    entry.getVerticalValues(),
                    entry.getVerticalSRID(),
                    extent);
            /*
             * Adds the entries for each image found in the file.
             * There is often only one image per file, but not always.
             */
            statement.setString(bySeries, series.getName());
            statement.setString(byFilename, entry.filename);
            statement.setString(byExtent,   extent);
            if (explicitTranslate) {
                final Point translate = entry.getGridOffset();
                statement.setInt(byDx, translate.x);
                statement.setInt(byDy, translate.y);
            }
            final DateRange[] dates = entry.getDateRanges();
            if (dates == null) {
                statement.setInt (byIndex,     1);
                statement.setNull(byStartTime, Types.TIMESTAMP);
                statement.setNull(byEndTime,   Types.TIMESTAMP);
                if (updateSingleton(statement)) count++;
            } else for (int i=0; i<dates.length; i++) {
                final Date startTime = dates[i].getMinValue();
                final Date   endTime = dates[i].getMaxValue();
                statement.setInt      (byIndex,     i + 1);
                statement.setTimestamp(byStartTime, new Timestamp(startTime.getTime()), calendar);
                statement.setTimestamp(byEndTime,   new Timestamp(endTime  .getTime()), calendar);
                if (updateSingleton(statement)) count++;
            }
            entry.close();
        }
        series = oldSeries;
        return count;
    }

    /**
     * Adds the specified tiles in the {@code "Tiles"} table.
     *
     * @param  tiles The tiles to insert.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public synchronized void addTiles(final Collection<Tile> tiles)
            throws CatalogException, SQLException, IOException
    {
        if (tilesTable == null) {
            // Uses the special GridCoverageQuery constructor for insertions in "Tiles" table.
            tilesTable = new WritableGridCoverageTable(new GridCoverageQuery(getDatabase(), true)) {
                @Override
                protected WritableGridCoverageEntry createEntry(Tile tile) throws IOException {
                    return WritableGridCoverageTable.this.createEntry(tile);
                }

                @Override
                protected WritableGridCoverageEntry createEntry(ImageReader reader, int imageIndex) throws IOException {
                    return WritableGridCoverageTable.this.createEntry(reader, imageIndex);
                }
            };
        }
        tilesTable.setLayer(getLayer());
        tilesTable.series = series;
        tilesTable.addEntries(tiles, 0);
    }

    /**
     * Searchs for new files in the {@linkplain #getLayer current layer} and {@linkplain #addEntries
     * adds} them to the database. The {@link #setLayer(Layer) setLayer} method must be invoked prior
     * this method. This method will process every {@linkplain Series series} for the current layer.
     *
     * @param  includeSubdirectories If {@code true}, then sub-directories will be included
     *         in the scan. New series may be created if subdirectories are found.
     * @param  policy The action to take for existing entries.
     * @return The number of images inserted.
     * @throws CatalogException If a logical error occured.
     * @throws SQLException If an error occured while querying the database.
     * @throws IOException If an I/O operation was required and failed.
     */
    public synchronized int updateLayer(final boolean includeSubdirectories, final UpdatePolicy policy)
            throws CatalogException, SQLException, IOException
    {
        final boolean replaceExisting = !UpdatePolicy.SKIP_EXISTING.equals(policy);
        final Layer layer = getNonNullLayer();
        Set<CoverageReference> coverages = null;
        final Map<Object,Series> inputs = new LinkedHashMap<Object,Series>();
        for (final Series series : layer.getSeries()) {
            /*
             * The inputs map will contains File or URI objects. If the protocol is "file",
             * we will scan the directory and put File objects in the map. Otherwise and if
             * the user asked for the replacement of existing file, we just copy the set of
             * existing URI. Otherwise we do nothing since we can't get the list of new items.
             */
            if (!series.getProtocol().equalsIgnoreCase("file")) {
                if (replaceExisting) {
                    if (coverages == null) {
                        coverages = layer.getCoverageReferences();
                    }
                    for (final CoverageReference coverage : coverages) {
                        if (series.equals(coverage.getSeries())) {
                            inputs.put(coverage.getURI(), series);
                        }
                    }
                }
                continue;
            }
            File directory = series.file("*");
            if (directory != null) {
                final String filename = directory.getName();
                final int split = filename.lastIndexOf('*');
                final String extension = (split >= 0) ? filename.substring(split + 1) : "";
                final FileFilter filter = new FileFilter() {
                    public boolean accept(final File file) {
                        if (file.isDirectory()) {
                            return includeSubdirectories;
                        }
                        return file.getName().endsWith(extension);
                    }
                };
                directory = directory.getParentFile();
                if (directory != null) {
                    final File[] list = directory.listFiles(filter);
                    if (list != null) {
                        addFiles(inputs, list, filter, series);
                        continue;
                    }
                }
            }
            LOGGER.warning(Resources.format(ResourceKeys.ERROR_DIRECTORY_NOT_FOUND_$1, directory.getPath()));
        }
        /*
         * We now have a list of every files found in the directories. Now remove the files that
         * are already present in the database. We perform this removal here instead than during
         * the directories scan in order to make sure that we don't query the database twice for
         * the same files (our usage of hash map ensures this condition).
         */
        int count = 0;
        boolean success = false;
        final Series oldSeries = series;
        transactionBegin();
        try {
            if (UpdatePolicy.CLEAR_BEFORE_UPDATE.equals(policy)) {
                clear();
            } else for (final Iterator<Map.Entry<Object,Series>> it=inputs.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<Object,Series> entry = it.next();
                final Object input = entry.getKey();
                final File file;
                if (input instanceof File) {
                    file = (File) input;
                } else if (input instanceof URL) {
                    file = new File(((URL) input).getPath());
                } else if (input instanceof URI) {
                    file = new File(((URI) input).getPath());
                } else {
                    continue;
                }
                String filename = file.getName();
                final int split = filename.lastIndexOf('.');
                if (split >= 0) {
                    filename = filename.substring(0, split);
                }
                series = entry.getValue();
                if (replaceExisting) {
                    delete(filename);
                } else if (exists(filename)) {
                    it.remove();
                }
            }
            /*
             * Now process to the insertions in the database.
             *
             * TODO: We need to decide what to do with new series (i.e. when series==null.
             *       In current state of affairs, we will get a NullPointerException).
             */
            Iterator<Map.Entry<Object,Series>> it;
            while ((it = inputs.entrySet().iterator()).hasNext()) {
                Map.Entry<Object,Series> entry = it.next();
                series = entry.getValue();
                final Object next = entry.getKey();
                it.remove();
                final int imageIndex = 0; // TODO: Do we have a better value to provide?
                final WritableGridCoverageIterator iterator =
                        new WritableGridCoverageIterator(this, series, imageIndex, it, next);
                count += addEntriesUnsafe(iterator);
            }
            success = true; // Must be the very last line in the try block.
        } finally {
            series = oldSeries;
            transactionEnd(success);
        }
        return count;
    }

    /**
     * Adds the {@code toAdd} files or directories to the specified map. This
     * method invokes itself recursively in order to scan for subdirectories.
     *
     * @param  files  The map in which to add the files.
     * @param  toAdd  The files or directories to add. This list will be sorted in place.
     * @param  filter The filename filter, or {@code null} for including all files.
     * @param  series The series to use as value in the map.
     */
    private static void addFiles(final Map<Object,Series> files, final File[] toAdd,
                                 final FileFilter filter, final Series series)
    {
        Arrays.sort(toAdd);
        for (final File file : toAdd) {
            final File[] list = file.listFiles(filter);
            if (list != null) {
                // If scanning sub-directories, invokes this method recursively but without
                // assigning series, since we will need to create a new series entry.
                addFiles(files, list, filter, null);
            } else {
                final Series old = files.put(file, series);
                if (old != null) {
                    // If this filename was already assigned to a series, keep the old entry.
                    // It is more likely to occurs if we are scanning sub-directories while
                    // one of those sub-directories is already used by an existing series.
                    files.put(file, old);
                }
            }
        }
    }

    /**
     * Creates an entry for the given tile to be inserted in the database. This method is invoked
     * automatically by {@link #addEntries}. The default implementation returns an instance created
     * from the {@link WritableGridCoverageEntry#WritableGridCoverageEntry(Tile) constructor with
     * same signature}. Subclasses can override this method in order to return some entries filled
     * with different metadata.
     *
     * @param  tile The tile to use for the entry.
     * @return The entry to be inserted into the database,
     *         or {@code null} if the given tile should be skipped.
     * @throws IOException if an error occured while reading the image.
     */
    protected WritableGridCoverageEntry createEntry(final Tile tile) throws IOException {
        return new WritableGridCoverageEntry(tile);
    }

    /**
     * Creates an entry for the given image to be inserted in the database. This method is invoked
     * automatically by {@link #addEntries}. The default implementation returns an instance created
     * from the {@link WritableGridCoverageEntry#WritableGridCoverageEntry(ImageReader,int)
     * constructor with same signature}. Subclasses can override this method in order to return
     * some entries filled with different metadata.
     *
     * @param  reader     The reader where to fetch metadata from.
     * @param  imageIndex The index of the image to be read.
     * @return The entry to be inserted into the database,
     *         or {@code null} if the given image should be skipped.
     * @throws IOException if an error occured while reading the image.
     */
    protected WritableGridCoverageEntry createEntry(final ImageReader reader, final int imageIndex)
            throws IOException
    {
        return new WritableGridCoverageEntry(reader, imageIndex);
    }

    /**
     * Flushs the cache.
     */
    @Override
    public synchronized void flush() {
        if (tilesTable != null) {
            tilesTable.flush();
        }
        super.flush();
    }
}
