/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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

import java.util.Map;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * An iterator over the same {@link ImageReader} instance, but set with different input source
 * at each iteration. This is for internal usage by {@link WritableGridCoverageTable} only.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ReaderIterator implements Iterator<ImageReader> {
    /**
     * The series in which the images will be added.
     */
    final Series series;

    /**
     * The image reader to use.
     */
    private final ImageReader reader;

    /**
     * An iterator over the files or URIs to read, relative to current directory.
     * The {@link Iterator#remove} method will be invoked during each iteration
     * (this is required by {@link WritableGridCoverageTable}).
     */
    private final Iterator<Map.Entry<Object,Series>> files;

    /**
     * The next file or URI to read, or {@code null} if none.
     */
    private Object next;

    /**
     * Creates an iterator for the specified files using the specified format.
     *
     * @param  series The series in which the images will be added.
     * @param  file   The files to read.
     * @param  next   The first element from the given iterator.
     * @throws IIOException if the format was not found.
     */
    ReaderIterator(final Series series, final Iterator<Map.Entry<Object,Series>> files, final Object next)
            throws IIOException
    {
        this.series = series;
        this.files  = files;
        this.next   = next;
        final Format format = series.getFormat();
        if (format instanceof FormatEntry) {
            reader = ((FormatEntry) format).createImageReader();
            return;
        }
        // Fallback (should not occurs with our implementation)
        final String mimeType = format.getMimeType();
        final Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType);
        if (!readers.hasNext()) {
            throw new IIOException(Resources.format(ResourceKeys.ERROR_NO_IMAGE_FORMAT_$1, mimeType));
        }
        reader = readers.next();
        if (readers.hasNext()) {
            throw new IIOException(Resources.format(ResourceKeys.ERROR_TOO_MANY_IMAGE_FORMATS_$1, mimeType));
        }
    }

    /**
     * Returns {@code true} if there is more images to read.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Returns the image reader with the input set to the next image file to read.
     */
    public ImageReader next() {
        reader.setInput(next);
        next = null;
        while (files.hasNext()) {
            final Map.Entry<?,Series> entry = files.next();
            if (series.equals(entry.getValue())) {
                next = entry.getKey();
                files.remove();
                break;
            }
        }
        return reader;
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
