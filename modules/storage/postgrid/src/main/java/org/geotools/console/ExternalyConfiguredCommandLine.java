/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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
package org.geotools.console;

import java.awt.Point;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import org.geotoolkit.console.CommandLine;
import org.geotoolkit.geometry.Envelope2D;


/**
 * A command line configured by an external {@code .properties} file in addition of command-line
 * options. The values of the property file can be obtained from the various {@code get(String)}
 * methods provided in this class. All method invokes {@link System#exit} on failure.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExternalyConfiguredCommandLine extends CommandLine {
    /**
     * Properties read from the file specified on the command line.
     */
    protected final Properties properties;

    /**
     * Creates a new {@code CommandLine} instance from the given arguments.
     *
     * @param  args The command-line arguments.
     */
    protected ExternalyConfiguredCommandLine(final String[] args) {
        super(null, args);
        if (arguments.length != 1) {
            err.println("Missing argument: properties file");
            System.exit(ILLEGAL_ARGUMENT_EXIT_CODE);
            throw new AssertionError(); // Should never reach this point.
        }
        properties = new Properties();
        try {
            final InputStream in = new FileInputStream(arguments[0]);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            err.println(e);
            System.exit(IO_EXCEPTION_EXIT_CODE);
        }
    }

    /**
     * Returns the value for the given key as a boolean, or the specified default value if none.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @param  defaultValue The default value if the key was not found.
     * @return The value.
     */
    protected boolean getBoolean(final String key, final boolean defaultValue) {
        String text = getString(key);
        if (text != null) {
            text = text.trim().toLowerCase(locale);
            if (text.equals("true")  || text.equals("yes")) return true;
            if (text.equals("false") || text.equals("no"))  return false;
            err.print("Unrecognized boolean: ");
            err.println(text);
            System.exit(BAD_CONTENT_EXIT_CODE);
        }
        return defaultValue;
    }

    /**
     * Returns the value for the given key as a string, or {@code null} if none.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @return The value, or {@code null} if none.
     */
    protected String getString(final String key) {
        return (String) properties.remove(key);
    }

    /**
     * Returns a file from the set of properties, or {@code null} if none.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @return The file, or {@code null} if none.
     */
    protected File getFile(final String key) {
        final String text = getString(key);
        if (text == null) {
            return null;
        }
        return new File(text);
    }

    /**
     * Parses a text from the properties as a point in pixel coordinates.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @return The point, or {@code null} if none.
     */
    protected Point getPoint(final String key) {
        final String text = getString(key);
        if (text == null) {
            return null;
        }
        int i = 0;
        final Point point = new Point();
        final StringTokenizer tokens = new StringTokenizer(text);
        while (tokens.hasMoreTokens()) {
            int value;
            try {
                value = Integer.parseInt(tokens.nextToken());
            } catch (NumberFormatException e) {
                err.println(e);
                System.exit(BAD_CONTENT_EXIT_CODE);
                value = 0;
            }
            switch (i++) {
                case 0:  point.x = value; break;
                case 1:  point.y = value; break;
                default: break; // An exception will be thrown at the end of this method.
            }
        }
        ensureExpectedCount(2, i, text);
        return point;
    }

    /**
     * Parses a text from the properties as a dimension in pixel coordinates.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @return The dimension, or {@code null} if none.
     */
    protected Dimension getDimension(final String key) {
        final Point point = getPoint(key);
        if (point != null) {
            return new Dimension(point.x, point.y);
        }
        return null;
    }

    /**
     * Parses a text from the properties as an envelope in "real world" coordinates.
     * The entry will be <strong>removed</strong> from the set of {@linkplain #properties}.
     *
     * @param  key The key to look for in the properties.
     * @return The envelope, or {@code null} if none.
     */
    protected Envelope2D getEnvelope(final String key) {
        final String text = getString(key);
        if (text == null) {
            return null;
        }
        int i = 0;
        final Envelope2D envelope = new Envelope2D();
        final StringTokenizer tokens = new StringTokenizer(text);
        while (tokens.hasMoreTokens()) {
            double value;
            try {
                value = Double.parseDouble(tokens.nextToken());
            } catch (NumberFormatException e) {
                err.println(e);
                System.exit(BAD_CONTENT_EXIT_CODE);
                value = Double.NaN;
            }
            switch (i++) {
                case 0:  envelope.x = value; break;
                case 1:  envelope.y = value; break;
                case 2:  envelope.width  = value - envelope.x; break;
                case 3:  envelope.height = value - envelope.y; break;
                default: break; // An error will be reported at the end of this method.
            }
        }
        ensureExpectedCount(4, i, text);
        return envelope;
    }

    /**
     * Ensures that we got the expected number of points.
     */
    private void ensureExpectedCount(final int expected, final int actual, final String text) {
        if (expected != actual) {
            err.print("Expected ");
            err.print(expected);
            err.print(" values but found ");
            err.print(actual);
            err.print(" in \"");
            err.print(text);
            err.println('"');
            System.exit(BAD_CONTENT_EXIT_CODE);
        }
    }

    /**
     * Ensures that there is no remaining entry in the set of {@linkplain #properties}. This
     * method is typically invoked after every entry has been processed and removed through
     * calls to {@code get(String)} methods. Any remaining entries would be unknown keys, in
     * which case it may be safer to report an error than ignoring them.
     */
    protected void ensureEmptyProperties() {
        if (!properties.isEmpty()) {
            err.println("Unknown properties:");
            for (final Object key : properties.keySet()) {
                err.print("    ");
                err.println(key);
            }
            System.exit(BAD_CONTENT_EXIT_CODE);
        }
    }
}
