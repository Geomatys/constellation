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
package net.sicade.coverage.catalog;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.geotools.resources.Utilities;
import net.sicade.catalog.Entry;


/**
 * A series entry.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SeriesEntry extends Entry implements Series {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -7991804359597967276L;

    /**
     * The layer which contains this series.
     */
    private final Layer layer;

    /**
     * Root images directory, for access through a local network.
     * May be {@code null} if the file are not accessible locally.
     */
    private final String rootDirectory;

    /**
     * Root URL directory (usually a FTP server), for access through a distant network.
     */
    private final String rootURL;

    /**
     * The relative or absolute directory which contains the data files for this series.
     * The path separator is Unix slash, never the Windows backslash. This path may be
     * relative to a root directory or to a base URL.
     */
    final String pathname;

    /**
     * The extension to add to filenames, not including the dot character.
     */
    final String extension;

    /**
     * The URL encoding (typically {@code "UTF-8"}), or {@code null} if no encoding should be done.
     * Note that this is the encoding for the URL itself, not the URL content.
     */
    private final String encoding;

    /**
     * The format of all coverages in this series.
     */
    private final Format format;
    
    /**
     * {@code true} if this series should be included in {@link Layer#getSeries}.
     */
    final boolean visible;

    /**
     * Creates a new series entry.
     *
     * @param name      The name for this series.
     * @param layer     The layer which contains this series.
     * @param pathname  The relative or absolute directory which contains the data files for this series.
     * @param extension The extension to add to filenames, not including the dot character.
     * @param format    The format of all coverages in this series.
     * @param visible   {@code true} if this series should be included in {@link Layer#getSeries}.
     * @param remarks   The remarks, or {@code null} if none.
     */
    protected SeriesEntry(final String name, final Layer layer, final String rootDirectory,
                          final String rootURL, final String pathname, final String extension,
                          final String encoding, final Format format, final boolean visible,
                          final String remarks)
{
        super(name, remarks);
        this.layer         = layer;
        this.rootDirectory = rootDirectory;
        this.rootURL       = rootURL;
        this.pathname      = pathname;
        this.extension     = extension;
        this.encoding      = encoding;
        this.format        = format;
        this.visible       = visible;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Layer getLayer() {
        return layer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Format getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File file(final String filename) {
        File file = new File(pathname, filename + '.' + extension);
        if (rootDirectory != null && !file.isAbsolute()) {
            file = new File(rootDirectory, file.getPath());
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL url(final File file) throws IOException {
        if (rootURL != null && !file.isAbsolute()) {
            final StringBuilder buffer = new StringBuilder(rootURL);
            final int last = buffer.length() - 1;
            if (last >= 0) {
                if (buffer.charAt(last) == '/') {
                    buffer.setLength(last);
                }
            }
            encodeURL(file, buffer, encoding);
            return new URL(buffer.toString());
        }
        return file.toURI().toURL();
    }

    /**
     * Converts a path to a URL. If {@code encoding} is not null, then the path is encoded.
     * This method invokes itself recursively for encoding the parents as well.
     */
    private static void encodeURL(final File path, final StringBuilder buffer, final String encoding)
            throws UnsupportedEncodingException
    {
        final File parent = path.getParentFile();
        if (parent != null) {
            encodeURL(parent, buffer, encoding);
        }
        buffer.append('/');
        String name = path.getName();
        if (encoding != null) {
            name = URLEncoder.encode(name, encoding);
        }
        buffer.append(name);
    }

    /**
     * Compare this series entry with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SeriesEntry that = (SeriesEntry) object;
            return Utilities.equals(this.layer,         that.layer )        &&
                   Utilities.equals(this.rootDirectory, that.rootDirectory) &&
                   Utilities.equals(this.rootURL,       that.rootURL)       &&
                   Utilities.equals(this.pathname,      that.pathname)      &&
                   Utilities.equals(this.extension,     that.extension)     &&
                   Utilities.equals(this.encoding,      that.encoding)      &&
                   Utilities.equals(this.format,        that.format);
        }
        return false;
    }
}
