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
package net.seagis.coverage.catalog;

import java.awt.Point;
import java.util.Map;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.lang.reflect.UndeclaredThrowableException;
import org.geotools.image.io.mosaic.Tile;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * An iterator creating {@link WritableGridCoverageEntry} on-the-fly using different input source.
 * The iterator reuse a unique {@link ImageReader} instance when possible. Each iterator is for a
 * single {@link Series} only.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class WritableGridCoverageIterator implements Iterator<WritableGridCoverageEntry> {
    /**
     * The table that created this iterator.
     */
    private final WritableGridCoverageTable table;

    /**
     * The series in which the images will be added, or {@code null} if unknown.
     */
    private final Series series;

    /**
     * Index of image to read. Ignored if the inputs are {@link Tile} instances.
     */
    private final int imageIndex;

    /**
     * An iterator over the inputs to read. If elements are {@link java.util.Map.Entry}, then the
     * key is selected as the input provided that the value is equals to the {@linkplain #series}.
     * Otherwise the entry is discarted.
     * <p>
     * If {@link #series} if non-null, the {@link Iterator#remove} method will be invoked for each
     * elements which has not been omitted. This is required by {@link WritableGridCoverageTable}.
     * <p>
     * If input are {@link File} or {@link URI}, they shall be relative to current directory.
     * Inputs may also be {@link Tile} or {@link ImageReader} instances.
     */
    private final Iterator<?> files;

    /**
     * The next entry to return, or {@code null} if we have reached the iteration end.
     */
    private WritableGridCoverageEntry next;

    /**
     * The image reader to use. Will be created when first needed. May never be created if every
     * inputs (the keys in the {@link #files} iterator) are {@link ImageReader} or {@link Tile}
     * instances.
     */
    private ImageReader reader;

    /**
     * Creates an iterator for the specified files.
     *
     * @param  table      The table that created this iterator.
     * @param  series     The series in which the images will be added, or {@code null} if unknown.
     * @param  imageIndex Index of images to read. Ignored if the inputs are {@link Tile} instances.
     * @param  files      The files to read. Iteration shall be at the second element.
     * @param  input      The first element from the given iterator.
     * @throws IOException if an I/O operation was required and failed.
     */
    WritableGridCoverageIterator(final WritableGridCoverageTable table,
                                 final Series series, final int imageIndex,
                                 final Iterator<?> files, Object input)
            throws IOException
    {
        this.table      = table;
        this.series     = series;
        this.imageIndex = imageIndex;
        this.files      = files;
        do {
            next = createEntry(input);
        } while (next == null && (input = nextInput()) != null);
    }

    /**
     * Returns the unique image reader. The reader is created the first time
     * this method is invoked, and reused for every subsequent invocations.
     * If no reader can be inferred because the {@linkplain #series} is not
     * specified, then this method returns {@code null}.
     */
    private ImageReader getImageReader() throws IOException {
        if (reader == null && series != null) {
            final Format format = series.getFormat();
            if (format instanceof FormatEntry) {
                reader = ((FormatEntry) format).getImageReaderSpi().createReaderInstance();
            } else {
                // Fallback (should not occurs with our implementation)
                final String formatName = format.getImageFormat();
                final Iterator<ImageReader> readers;
                if (formatName.indexOf('/') >= 0) {
                    readers = ImageIO.getImageReadersByMIMEType(formatName);
                } else {
                    readers = ImageIO.getImageReadersByFormatName(formatName);
                }
                if (!readers.hasNext()) {
                    throw new IIOException(Resources.format(ResourceKeys.ERROR_NO_IMAGE_FORMAT_$1, formatName));
                }
                reader = readers.next();
                if (readers.hasNext()) {
                    throw new IIOException(Resources.format(ResourceKeys.ERROR_TOO_MANY_IMAGE_FORMATS_$1, formatName));
                }
            }
        }
        return reader;
    }

    /**
     * Returns {@code true} if there is more entry to iterate over.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Creantes an entry for the given input.
     *
     * @param  input The input, or {@code null} if none.
     * @return The entry, or {@code null} if the given input should be skipped.
     * @throws IOException if an I/O operation was required and failed.
     */
    private WritableGridCoverageEntry createEntry(final Object input) throws IOException {
        if (input == null) {
            return null;
        }
        if (input instanceof Tile) {
            return table.createEntry((Tile) input);
        }
        final ImageReader reader;
        if (input instanceof ImageReader) {
            reader = (ImageReader) input;
        } else {
            reader = getImageReader();
            if (reader != null) {
                reader.setInput(input);
            } else {
                // Lets the Tile constructor figure out the provider by itself.
                final Tile tile = new Tile(null, next, imageIndex, new Point(0,0), null);
                return table.createEntry(tile);
            }
        }
        return table.createEntry(reader, imageIndex);
    }

    /**
     * Returns the next entry to add to the database.
     */
    public WritableGridCoverageEntry next() {
        final WritableGridCoverageEntry entry = next;
        if (entry == null) {
            throw new NoSuchElementException();
        }
        next = null;
        do {
            final Object input = nextInput();
            if (input == null) {
                break;
            }
            try {
                next = createEntry(input);
            } catch (IOException exception) {
                // Will be unwrapped by WritableGridCoverageTable.
                throw new UndeclaredThrowableException(exception);
            }
        } while (next == null);
        entry.series = series;
        return entry;
    }

    /**
     * Returns the next input (omitting {@code null} values), or {@code null} if we have
     * reached the iteration end.
     *
     * @return The next input, or {@code null} if we have reached iteration end.
     */
    private Object nextInput() {
        while (files.hasNext()) {
            Object input = files.next();
            if (input != null) {
                if (input instanceof Map.Entry) {
                    final Map.Entry<?,?> candidate = (Map.Entry) input;
                    if (series != null && !series.equals(candidate.getValue())) {
                        continue;
                    }
                    input = candidate.getKey();
                }
                if (series != null) {
                    files.remove();
                }
                return input;
            }
        }
        return null;
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
        buffer.append('[');
        if (series != null) {
            buffer.append("series=\"").append(series).append("\", ");
        }
        if (reader != null) {
            buffer.append("reader=").append(reader.getClass().getSimpleName()).append(", ");
        }
        return buffer.append("imageIndex=").append(imageIndex).append(']').toString();
    }
}
