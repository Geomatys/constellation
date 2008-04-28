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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.resources.Utilities;
import net.seagis.catalog.Entry;


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
    private static final long serialVersionUID = 7119677073947466143L;

    /**
     * The layer which contains this series.
     */
    private final Layer layer;

    /**
     * The protocol in a URL, or {@code "file"} if the files should be read locally.
     */
    private final String protocol;

    /**
     * The host in a URL, or {@code null} if the files should be read locally.
     */
    private final String host;

    /**
     * The directory which contains the data files for this series.
     * The path separator is Unix slash, never the Windows backslash.
     * May be an empty string but never {@code null}.
     */
    private final String path;

    /**
     * The extension to add to filenames, not including the dot character.
     */
    private final String extension;

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
     * @param root      The root directory or URL, or {@code null} if none.
     * @param pathname  The relative or absolute directory which contains the data files for this series.
     * @param extension The extension to add to filenames, not including the dot character.
     * @param format    The format of all coverages in this series.
     * @param visible   {@code true} if this series should be included in {@link Layer#getSeries}.
     * @param remarks   The remarks, or {@code null} if none.
     */
    protected SeriesEntry(final String name, final Layer layer, final String root,
                          final String pathname, final String extension, final Format format,
                          final boolean visible, final String remarks)
{
        super(name, remarks);
        this.layer     = layer;
        this.extension = extension;
        this.format    = format;
        this.visible   = visible;
        /*
         * Checks if the pathname contains a URL host.  If it does, then this URL will have
         * precedence over the root parameter. The following section make this check, which
         * ignore totally the root parameter.
         */
        int split = pathname.indexOf("://");
        if (split >= 0) {
            protocol = pathname.substring(0, split).trim();
            split += 3;
            if (protocol.equalsIgnoreCase("file")) {
                host = null;
                path = pathname.substring(split);
                // Path is likely to contains a leading '/' since the syntax is usualy "file:///".
            } else {
                final int base = split;
                split = pathname.indexOf('/', split);
                if (split < 0) {
                    // No host after the protocol (e.g. "dods://www.foo.org").
                    host = pathname.substring(base);
                    path = "";
                } else {
                    host = pathname.substring(base, split);
                    path = pathname.substring(++split);
                }
            }
            return;
        }
        /*
         * Below this point, we known that the pathname is not an URL.
         * but maybe the "root" parameter is an URL. Checks it now.
         */
        if (root == null) {
            protocol = "file";
            host     = null;
            path     = pathname;
            return;
        }
        split = root.indexOf("://");
        if (split < 0) {
            protocol = "file";
            host     = null;
            split    = 0; // Used for computing 'path' later.
        } else {
            protocol = root.substring(0, split).trim();
            split += 3;
            if (protocol.equalsIgnoreCase("file")) {
                host = null;
            } else {
                final int base = split;
                split = root.indexOf('/', split);
                if (split < 0) {
                    host = root.substring(base);
                    path = pathname;
                    return;
                }
                host = root.substring(base, split++);
            }
        }
        final boolean isAbsolute = pathname.startsWith("/");
        if (isAbsolute) {
            path = pathname;
        } else {
            final String directory = root.substring(split);
            final StringBuilder buffer = new StringBuilder(directory);
            if (!directory.endsWith("/")) {
                buffer.append('/');
            }
            path = buffer.append(pathname).toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * {@inheritDoc}
     */
    public Format getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * {@inheritDoc}
     */
    public File file(String filename) {
        if (extension != null && extension.length() != 0) {
            filename = filename + '.' + extension;
        }
        return new File(path, filename);
    }

    /**
     * {@inheritDoc}
     */
    public URI uri(final String filename) throws URISyntaxException {
        if (host == null) {
            return file(filename).toURI();
        }
        final StringBuilder buffer = new StringBuilder();
        if (!path.startsWith("/")) {
            buffer.append('/');
        }
        buffer.append(path);
        if (!path.endsWith("/") && !filename.startsWith("/")) {
            buffer.append('/');
        }
        buffer.append(filename);
        if (extension != null && extension.length() != 0) {
            buffer.append('.').append(extension);
        }
        return new URI(protocol, host, buffer.toString(), null);
    }

    /**
     * Compares this series entry with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SeriesEntry that = (SeriesEntry) object;
            return Utilities.equals(this.layer,     that.layer )    &&
                   Utilities.equals(this.protocol,  that.protocol)  &&
                   Utilities.equals(this.host,      that.host)      &&
                   Utilities.equals(this.path,      that.path)      &&
                   Utilities.equals(this.extension, that.extension) &&
                   Utilities.equals(this.format,    that.format);
        }
        return false;
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder("SeriesEntry:");
        s.append(name).append("visible:").append(visible);
        return s.toString();
    }
}
