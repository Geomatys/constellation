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

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.Closeable;
import java.net.URL;
import java.net.URI;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.sql.SQLException;
import java.util.Date;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;
import javax.measure.unit.SI;

import org.geotools.util.DateRange;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.image.io.mosaic.Tile;

import org.constellation.catalog.Database;
import org.constellation.catalog.CatalogException;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * An entry to be written in a {@link WritableGridCoverageTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class WritableGridCoverageEntry {
    /**
     * The series in which the images will be added, or {@code null} if unknown.
     */
    Series series;

    /**
     * Contains most informations like image reader provider, input, image index and image bounds.
     */
    private final Tile tile;

    /**
     * The image reader ready for use, or {@code null} if all information should be fetch
     * from the {@linkplain #tile}.
     */
    private final ImageReader reader;

    /**
     * The path to the coverage file (not including the filename). This is part of the input
     * splitted as "{@linkplain #path}/{@linkplain #filename}.{@linkplain #extension}". May
     * be {@code null} if the filename has no parent directory.
     */
    protected final File path;

    /**
     * The filename, not including the path or the extension. This is part of the input
     * splitted as "{@linkplain #path}/{@linkplain #filename}.{@linkplain #extension}".
     */
    protected final String filename;

    /**
     * The filename extension, not including the leading dot. This is part of the input
     * splitted as "{@linkplain #path}/{@linkplain #filename}.{@linkplain #extension}".
     * May be empty but never null.
     */
    protected final String extension;

    /**
     * The object to use for parsing image metadata. This is set by {@link #parseMetadata}.
     */
    private MetadataParser metadata;

    /**
     * Creates an entry for the given tile.
     *
     * @param  tile The tile to use for the entry.
     * @throws IOException if an error occured while reading the image.
     */
    public WritableGridCoverageEntry(final Tile tile) throws IOException {
        this(tile, exists(tile.getInput()) ? tile.getImageReader() : null);
    }

    /**
     * Creates en entry for the given reader.
     *
     * @param  reader The image reader.
     * @param  imageIndex Index of the image to read.
     * @throws IOException if an error occured while reading the image.
     */
    public WritableGridCoverageEntry(final ImageReader reader, final int imageIndex) throws IOException {
        this(new Tile(reader.getOriginatingProvider(), reader.getInput(), imageIndex,
             new Rectangle(reader.getWidth(imageIndex), reader.getHeight(imageIndex))), reader);
    }

    /**
     * Creates en entry for the given reader.
     *
     * @param  tile Information about the image to be inserted.
     * @param  reader The image reader, or {@code null}.
     * @throws IIOException if the tile input is unknown.
     */
    private WritableGridCoverageEntry(final Tile tile, final ImageReader reader) throws IIOException {
        this.tile   = tile;
        this.reader = reader;
        final Object input = tile.getInput();
        final File inputFile;
        if (input instanceof File) {
            inputFile = (File) input;
        } else if (input instanceof URL) {
            inputFile = new File(((URL) input).getPath());
        } else if (input instanceof URI) {
            inputFile = new File(((URI) input).getPath());
        } else if (input instanceof CharSequence) {
            inputFile = new File(input.toString());
        } else {
            throw new IIOException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1,
                    Classes.getShortClassName(input)));
        }
        path = inputFile.getParentFile();
        final String name = inputFile.getName();
        final int split = name.lastIndexOf('.');
        if (split >= 0) {
            filename  = name.substring(0, split);
            extension = name.substring(split + 1);
        } else {
            filename  = name;
            extension = "";
        }
    }

    /**
     * Returns {@code true} if the given input is an existing file.
     */
    private static boolean exists(final Object input) {
        return (input instanceof File) && ((File) input).isFile();
    }

    /**
     * Returns the most appropriate series in which to insert the coverage.
     * This is heuristic rules used when no series was explicitly defined.
     * <p>
     * Note: if {@link #extension} is {@code null} (not the same as an empty string),
     * it is interpreted as "any extension".
     *
     * @param  candidates The series to consider.
     * @return The series that seems the best match.
     * @throws CatalogException if there is ambiguity between series.
     */
    final Series choose(final Collection<Series> candidates) throws CatalogException {
        series = null;
        int mimeMatching = 0; // Greater the number, better is the matching of MIME type.
        int pathMatching = 0; // Greater the number, better is the matching of the file path.
        final ImageReaderSpi spi = tile.getImageReaderSpi();
        for (final Series candidate : candidates) {
            /*
             * Asks for every files in the Series directory (e.g. "/home/data/foo/*.png"). The
             * filename contains a wildcard, but we will not use that. It is just a way to get
             * the path & extension, so we can check if the series have the expected extension.
             */
            final File allFiles = candidate.file("*");
            String name = allFiles.getName();
            final int split = name.lastIndexOf('.');
            final String ext = (split >= 0) ? name.substring(split + 1) : "";
            if (!extension.equalsIgnoreCase(ext)) {
                continue;
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
                final String[] formatNames = spi.getFormatNames();
                final String format = candidate.getFormat().getImageFormat().trim().toLowerCase();
                final String[] names = (mimeTypes != null && format.indexOf('/') >= 0) ? mimeTypes : formatNames;
                for (String type : names) {
                    type = type.trim().toLowerCase();
                    final int length = type.length();
                    if (length > mimeMatching && format.startsWith(type)) {
                        mimeMatching = length;
                        pathMatching = 0; // Format matching has precedence over path matching.
                        // Consequence of above, series will be assigned in the check for pathname below.
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
            while (f1 != null && f2 != null && f1.getName().equals(f2.getName())) {
                depth++;
                f1 = f1.getParentFile();
                f2 = f2.getParentFile();
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
     * Returns the format name. We select the longuest one (since we assume that it is the most
     * explicit) that do not contains the {@code '/'} character. The later condition is because
     * we use {@code '/'} as an heuristic way to differentiate MIME type from format name.
     *
     * @param  upper {@code true} for the upper-case flavor (if available).
     * @return The format name (never {@code null} and never empty).
     * @throws IOException if the format can not be obtained.
     */
    protected String getFormatName(final boolean upper) throws IOException {
        String format = "";
        final ImageReaderSpi spi = tile.getImageReaderSpi();
        String[] formats = spi.getFormatNames();
        if (formats != null) {
            for (final String candidate : formats) {
                if (candidate.indexOf('/') < 0) {
                    final int d = candidate.length() - format.length();
                    if (d < 0) continue;
                    if (d == 0) {
                        if (!upper) continue;
                        int na=0, nb=0;
                        for (int i=candidate.length(); --i>=0;) {
                            if (Character.isUpperCase(candidate.charAt(i))) na++;
                            if (Character.isUpperCase(format   .charAt(i))) na++;
                        }
                        if (na <= nb) continue;
                    }
                    format = candidate;
                }
            }
        }
        if (format.length() == 0) {
            // No format found - fall back on mime types.
            formats = spi.getMIMETypes();
            if (formats != null) {
                for (final String candidate : formats) {
                    if (candidate.indexOf('/') >= 0 && candidate.length() > format.length()) {
                        format = candidate;
                    }
                }
            }
        }
        if (format.length() == 0) {
            throw new IOException("Unknown format."); // TODO: localize
        }
        return format;
    }

    /**
     * Parses metadata using the connection to the given database.
     *
     * @param  database The connection to the database.
     * @throws IOException if an error occured while reading the metadata.
     */
    final void parseMetadata(final Database database) throws IOException {
        if (reader != null) {
            metadata = new MetadataParser(database, reader, tile.getImageIndex());
        }
    }

    /**
     * Returns the image size.
     *
     * @return The image size.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public Dimension getImageSize() throws IOException, CatalogException {
        return tile.getRegion().getSize();
    }

    /**
     * Returns the date range for the given image metadata. This method usually returns a singleton,
     * but more than one time range could be returned if the image reader contains data at many times.
     *
     * @return The date range for the given metadata, or {@code null} if none.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public DateRange[] getDateRanges() throws IOException, CatalogException {
        return (metadata != null) ? metadata.getDateRanges() : null;
    }

    /**
     * Returns the date origin found during the last invocation of {@link #getDateRanges}.
     * Returns {@code null} if the later method has not been invoked or didn't completed
     * successfully.
     *
     * @return The date origin, or {@code null} if none.
     */
    protected Date getTimeOrigin() {
        return (metadata != null) ? metadata.timeOrigin : null;
    }

    /**
     * Returns the time units found during the last invocation of {@link #getDateRanges}.
     * Returns {@code null} if the later method has not been invoked or didn't completed
     * successfully.
     *
     * @return The time unit, or {@code null} if none.
     */
    protected Unit<Duration> getTimeUnit() {
        return (metadata != null) ? metadata.timeUnit : null;
    }

    /**
     * Returns the translation to apply on the grid before to apply the <cite>grid to CRS</cite>
     * transform. This is usually the {@linkplain Tile#getLocation location of a tile} in tiled
     * images.
     *
     * @return The grid offset.
     */
    public Point getGridOffset() {
        return (tile != null) ? tile.getLocation() : new Point();
    }

    /**
     * Returns the <cite>grid to CRS</cite> transform for the specified axis. The returned
     * transform maps always the pixel {@linkplain PixelOrientation#UPPER_LEFT upper left}
     * corner.
     *
     * @param  translate
     *          {@code true} if the transform should be {@linkplain AffineTransform#translate translated}
     *          by the {@linkplain #getGridOffset grid offset}. This argument is typically {@code false}
     *          if the entry will be inserted in a table with explicit (<var>dx</var>,<var>dy</var>)
     *          translation terms like the {@code Tiles} table, or {@code true} otherwise.
     * @return The affine transform from grid to CRS, or {@code null} if it can't be computed.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public AffineTransform getGridToCRS(final boolean translate)
            throws IOException, CatalogException
    {
        AffineTransform gridToCRS = tile.getGridToCRS();
        if (gridToCRS != null) {
            if (translate) {
                // The entry to be recorded in the database has its origin to (0,0).
                // If the tile has an other origin, we need to translate it accordingly.
                final Point origin = getGridOffset();
                if (origin.x != 0 || origin.y != 0) {
                    gridToCRS = new AffineTransform(gridToCRS);
                    gridToCRS.translate(origin.x, origin.y);
                }
            }
        } else if (metadata != null) {
            // No translation to apply here because the 'gridToCRS' transform
            // doesn't come from the tile.
            gridToCRS = metadata.getGridToCRS();
        }
        return gridToCRS;
    }

    /**
     * Returns the horizontal CRS identifier, or {@code 0} if unknown.
     *
     * @return The horizontal CRS identifier, or {@code 0} if unknown.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public int getHorizontalSRID() throws IOException, CatalogException {
        int srid = 0;
        if (metadata != null) try {
            srid = metadata.getHorizontalSRID();
        } catch (SQLException e) {
            throw new CatalogException(e);
        }
        return srid;
    }

    /**
     * Returns the vertical CRS identifier, or {@code 0} if unknown.
     *
     * @return The vertical CRS identifier, or {@code 0} if unknown.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public int getVerticalSRID() throws IOException, CatalogException {
        return (metadata != null) ? metadata.getVerticalSRID() : 0;
    }

    /**
     * Returns the vertical coordinate values, or {@code null} if none.
     *
     * @return The vertical values, or {@code null} if unknown.
     * @throws IOException if an error occured while reading the image metadata.
     * @throws CatalogException if a logical error occured.
     */
    public double[] getVerticalValues() throws IOException, CatalogException {
        if (metadata == null) {
            return null;
        }
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
        return verticalOrdinates;
    }

    /**
     * Closes the reader (but do not dispose it, since it may be used for the next entry).
     */
    final void close() throws IOException {
        if (reader != null) {
            final Object input = reader.getInput();
            reader.reset();
            if (input instanceof Closeable) {
                ((Closeable) input).close();
            } else if (input instanceof ImageInputStream) {
                ((ImageInputStream) input).close();
            }
        }
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(tile);
        if (series != null) {
            buffer.append(" in ").append(series);
        }
        return buffer.toString();
    }
}
