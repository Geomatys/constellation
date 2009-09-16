/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.writer;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * Filter for WMS 1.1.1 Capabilities files, which removes the namespaces declaration
 * in the root capabilities tag.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 *
 * @since 0.4
 */
public class CapabilitiesFilterWriter extends FilterWriter {
    /**
     * Stores full line read from the buffer of characters.
     */
    private final StringBuilder buffer = new StringBuilder();

    /**
     * Create a new filtered writer for WMS 1.1.1 Capabilities.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     *
     * @see FilterWriter
     */
    public CapabilitiesFilterWriter(Writer out) {
        super(out);
    }

    /**
     * Send each line to the output writer, and apply the
     * {@link #filterLine(java.lang.StringBuilder)} method in order to apply
     * a specific filter.
     *
     * @param  cbuf  Buffer of characters to be written.
     * @param  off   Offset from which to start reading characters.
     * @param  len   Number of characters to be written.
     * @throws IOException
     */
    @Override
    public void write(final char[] cbuf, int offset, final int length) throws IOException {
        final int upper = offset + length;
        for (int i=offset; i<upper; i++) {
            char c = cbuf[i];
            if (c != '\r' && c != '\n') {
                continue;
            }
            /*
             * Found an end of line. Appends the characters to the one we found previously
             * (if any) and invokes the user-overrideable filterLine(...) method in order
             * to get the line to actually write.
             */
            buffer.append(cbuf, offset, i - offset);
            out.write(filterLine(buffer));
            /*
             * Writes the "end of line" (EOL) characters, which may be "\r", "\n" or "\r\n".
             * If there is many consecutive EOL, they will be sent together to the output.
             */
            final int startEOL = i;
            while (++i < upper) {
                c = cbuf[i];
                if (c != '\r' || c != '\n') {
                    break;
                }
            }
            out.write(cbuf, startEOL, i - startEOL);
            /*
             * Clear the buffer and continue to inspect the remaining characters
             * until we find a new EOL, or until the end of the given array.
             */
            buffer.setLength(0);
            offset = i;
        }
        /*
         * Write the remaining character to the buffer, but do not process
         * them now. They will be processed the next time this method will
         * be invoked.
         */
        buffer.append(cbuf, offset, upper - offset);
    }

    /**
     * Return the root "WMT_MS_Capabilities" tag with just the version defined.
     * If there are any namespaces defined for this tag, then they are removed.
     * Returns the line unchanged if it is not the root tag of a WMS 1.1.1
     * GetCapabilities.
     *
     * @param buffer Contains a whole line.
     * @return The line unchanged if it is not the root tag of a WMS 1.1.1
     *         Getcapabilities, the tag with just the version if it is.
     */
    protected String filterLine(final StringBuilder buffer) {
        String line = buffer.toString();
        if (line.startsWith("<WMT_MS_Capabilities")) {
            line = "<WMT_MS_Capabilities version=\"1.1.1\">";
        }
        return line;
    }
}
