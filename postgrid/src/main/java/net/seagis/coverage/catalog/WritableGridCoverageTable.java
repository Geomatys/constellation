/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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

import java.sql.Types;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.units.SI;

import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.seagis.util.DateRange;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.CatalogException;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;


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
     * Constructs a new {@code WritableGridCoverageTable}.
     *
     * @param connection The connection to the database.
     */
    public WritableGridCoverageTable(final Database database) {
        super(database);
    }

    /**
     * Constructs a new {@code WritableGridCoverageTable} with the same initial configuration
     * than the specified table.
     */
    public WritableGridCoverageTable(final WritableGridCoverageTable table) {
        super(table);
    }

    /**
     * Returns the most appropriate series in which to insert the coverage.
     *
     * @param  spi The image reader provider, or {@code null} if unknown.
     * @param  path The path to the coverage file (not including the filename).
     * @param  extension The file extension, or {@code null} if it doesn't matter.
     * @return The series that seems the best match.
     * @throws CatalogException if there is ambiguity between series.
     */
    private Series getSeries(final ImageReaderSpi spi, final File path, final String extension)
            throws CatalogException
    {
        Series series = null;
        int mimeMatching = 0; // Greater the number, better is the matching of MIME type.
        int pathMatching = 0; // Greater the number, better is the matching of the file path.
        for (final Series candidate : getNonNullLayer().getSeries()) {
            /*
             * Asks for every files in the Series directory (e.g. "/home/data/foo/*.png"). The
             * filename contains a wildcard, but we will not use that. It is just a way to get
             * the path & extension, so we can check if the series have the expected extension.
             */
            final File allFiles = candidate.file("*");
            if (extension != null) {
                String name = allFiles.getName();
                final int split = name.indexOf('.');
                if (split >= 0) {
                    name = name.substring(split + 1);
                }
                if (!extension.equalsIgnoreCase(name)) {
                    continue;
                }
            }
            /*
             * Checks if the Series's MIME type matches one of the ImageReader's MIME types. If the
             * ImageReader declares more generic types than the expected one, for example if the
             * ImageReader declares "image/x-netcdf" while the Series expects "image/x-netcdf-foo",
             * we will accept the ImageReader anyway but we will keep a trace of the quality of the
             * matching, so we can select a better match if we find one later.
             */
            if (spi != null) {
                final String[] mimeTypes = spi.getMIMETypes();
                if (mimeTypes != null) {
                    final String format = candidate.getFormat().getMimeType().trim().toLowerCase();
                    for (String type : mimeTypes) {
                        type = type.trim().toLowerCase();
                        final int length = type.length();
                        if (length > mimeMatching && format.startsWith(type)) {
                            mimeMatching = length;
                            pathMatching = 0; // MIME matching has precedence over path matching.
                        }
                    }
                }
            }
            /*
             * The most straightforward properties match (file extension, mime type...).
             * Now check the path in a more lenient way: we compare the Series path with
             * the ImageReader input's path starting from the end, and retain the series
             * with the deepest (in directory tree) match. If more than one series match
             * with the same deep, we retains the last one assuming that it is the one
             * for the most recent data.
             */
            int depth = 0;
            File f1 = path;
            File f2 = allFiles.getParentFile();
            while (f1.getName().equals(f2.getName())) {
                depth++;
                f1 = f1.getParentFile(); if (f1 == null) break;
                f2 = f2.getParentFile(); if (f2 == null) break;
            }
            if (depth >= pathMatching) {
                pathMatching = depth;
                series = candidate;
            }
        }
        if (series == null) {
            throw new CatalogException(Resources.format(ResourceKeys.ERROR_NO_SERIES_SELECTION));
        }
        return series;
    }

    /**
     * Returns the path for the specified input. The returned file should not be opened
     * since it may be invalid (especially if built from a URL input). Its only purpose
     * is to split the name part and the path part.
     *
     * @param  input The input.
     * @return The input as a file.
     * @throws CatalogException if the input is not recognized.
     */
    private static File path(final Object input) throws CatalogException {
        if (input instanceof File) {
            return (File) input;
        }
        if (input instanceof URL) {
            return new File(((URL) input).getPath());
        }
        if (input instanceof CharSequence) {
            return new File(input.toString());
        }
        throw new CatalogException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, Utilities.getShortClassName(input)));
    }

    /**
     * Adds entries (usually only one) inferred from the specified image reader.
     * The {@linkplain ImageReader#getInput reader input} must be set, and its
     * {@linkplain ImageReader#getImageMetadata metadata} shall conforms to the
     * Geotools {@linkplain GeographicMetadata geographic metadata}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param readers The image reader.
     */
    public void addEntry(final ImageReader reader) throws CatalogException, SQLException, IOException {
        addEntries(Collections.singleton(reader).iterator(), 0);
    }

    /**
     * Adds entries inferred from the specified image readers. The {@linkplain ImageReader#getInput
     * reader input} must be set, and its {@linkplain ImageReader#getImageMetadata metadata} shall
     * conforms to the Geotools {@linkplain GeographicMetadata geographic metadata}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param readers    The image readers. The iterator may recycle the same reader with different
     *                   {@linkplain ImageReader#getInput input} on each call to {@link Iterator#next}.
     * @param imageIndex The index of the image to insert in the database.
     */
    public synchronized void addEntries(final Iterator<ImageReader> readers, final int imageIndex)
            throws CatalogException, SQLException, IOException
    {
        final Connection connection = getDatabase().getConnection();
        final boolean autoCommit = connection.getAutoCommit();
        boolean success = false;
        try {
            connection.setAutoCommit(false);
            insertEntries(readers, imageIndex);
            connection.commit();
            success = true;
        } finally {
            if (!success) {
                connection.rollback();
            }
            connection.setAutoCommit(autoCommit);
        }
    }

    /**
     * Adds entries without the protection provided by the database rollback mechanism.
     * The commit or rollback must be performed by the caller.
     */
    private void insertEntries(final Iterator<ImageReader> readers, final int imageIndex)
            throws CatalogException, SQLException, IOException
    {
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
        while (readers.hasNext()) {
            final ImageReader reader = readers.next();
            final File input = path(reader.getInput());
            final File path = input.getParentFile();
            final String filename, extension;
            if (true) {
                final String name = input.getName();
                final int split = name.lastIndexOf('.');
                if (split >= 0) {
                    filename  = name.substring(0, split);
                    extension = name.substring(split + 1);
                } else {
                    filename  = name;
                    extension = "";
                }
            }
            /*
             * If we are scanning new files for a specific series, gets that series.
             * Otherwise try to guess it from the path name and file extension.
             */
            final Series series;
            if (readers instanceof ReaderIterator) {
                series = ((ReaderIterator) readers).series;
            } else {
                series = getSeries(reader.getOriginatingProvider(), path, extension);
            }
            /*
             * Gets the metadata of interest.
             */
            final MetadataParser metadata = new MetadataParser(getDatabase(), reader, imageIndex);
            final DateRange[] dates = metadata.getDateRanges();
            final int width  = reader.getWidth (imageIndex);
            final int height = reader.getHeight(imageIndex);
            final AffineTransform gridToCRS = metadata.getGridToCRS();
            final int horizontalSRID = metadata.getHorizontalSRID();
            final int verticalSRID = metadata.getVerticalSRID();
            double[] verticalOrdinates = metadata.getVerticalValues(SI.METER);
            if (verticalOrdinates == null) {
                /*
                 * We tried to get the vertical coordinates in meters if possible, so conversions
                 * from other linear units like feet were applied if needed.   If such conversion
                 * was not possible, gets the coordinates in whatever units they are. It may be a
                 * pressure unit or a dimensionless unit (e.g. "sigma level").
                 *
                 * TODO: We need to revisit that. Maybe a different column for altitudes in meters
                 *       and altitudes in native units.
                 */
                verticalOrdinates = metadata.getVerticalValues(null);
            }
            final String extent = gridTable.getIdentifier(new Dimension(width, height),
                    gridToCRS, horizontalSRID, verticalOrdinates, verticalSRID, series.getName());
            /*
             * Adds the entries for each image found in the file.
             * There is often only one image per file, but not always.
             */
            statement.setString(bySeries, series.getName());
            statement.setString(byFilename, filename);
            statement.setString(byExtent,   extent);
            if (dates == null) {
                statement.setInt (byIndex,     1);
                statement.setNull(byStartTime, Types.TIMESTAMP);
                statement.setNull(byEndTime,   Types.TIMESTAMP);
                insertSingleton(statement);
            } else for (int i=0; i<dates.length; i++) {
                final Date startTime = dates[i].getMinValue();
                final Date   endTime = dates[i].getMaxValue();
                statement.setInt      (byIndex,     i + 1);
                statement.setTimestamp(byStartTime, new Timestamp(startTime.getTime()), calendar);
                statement.setTimestamp(byEndTime,   new Timestamp(endTime  .getTime()), calendar);
                insertSingleton(statement);
            }
        }
    }

    /**
     * Searchs for new files in the {@linkplain #getLayer current layer} and {@linkplain #addEntries
     * adds} them to the database. The {@link #setLayer(Layer) setLayer} method must be invoked prior
     * this method. This method will process every {@linkplain Series series} for the current layer.
     *
     * @param includeSubdirectories If {@code true}, then sub-directories will be included
     *        in the scan. New series may be created if subdirectories are found.
     */
    public synchronized void completeLayer(final boolean includeSubdirectories)
            throws CatalogException, SQLException, IOException
    {
        final Map<File,Series> files = new LinkedHashMap<File,Series>();
        for (final Series series : getLayer().getSeries()) {
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
                        add(files, list, filter, series);
                        continue;
                    }
                }
            }
            LOGGER.warning("Le répertoire de la série \"" + series + "\" n'a pas été trouvé."); // TODO: localize
        }
        /*
         * We now have a list of every files found in the directories. Now remove the files that
         * are already present in the database. We perform this removal here instead than during
         * the directories scan in order to make sure that we don't query the database twice for
         * the same files (our usage of hash map ensures this condition).
         */
        for (final Iterator<File> it=files.keySet().iterator(); it.hasNext();) {
            final File file = it.next();
            String filename = file.getName();
            final int split = filename.lastIndexOf('.');
            if (split >= 0) {
                filename  = filename.substring(0, split);
            }
            // TODO: 'exists' takes only the layer in account, not the series.
            if (exists(filename)) {
                it.remove();
            }
        }
        /*
         * Now process to the insertions in the database.
         * 
         * TODO: We need to decide what to do with new series (i.e. when series==null.
         *       In current state of affairs, we will get a NullPointerException).
         */
        Iterator<Map.Entry<File,Series>> it;
        while ((it = files.entrySet().iterator()).hasNext()) {
            Map.Entry<File,Series> entry = it.next();
            final Series series = entry.getValue();
            File next = entry.getKey();
            it.remove();
            final Iterator<ImageReader> iterator = new ReaderIterator(series, it, next);
            addEntries(iterator, 0); // TODO: is ther better value to provide for imageIndex?
        }
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
    private static void add(final Map<File,Series> files, final File[] toAdd,
                            final FileFilter filter, final Series series)
    {
        Arrays.sort(toAdd);
        for (final File file : toAdd) {
            final File[] list = file.listFiles(filter);
            if (list != null) {
                // If scanning sub-directories, invokes this method recursively but without
                // assigning series, since we will need to create a new series entry.
                add(files, list, filter, null);
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
}
