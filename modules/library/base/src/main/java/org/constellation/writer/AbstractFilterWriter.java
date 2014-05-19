/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.writer;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * Provides a {@link #filterLine(java.lang.StringBuilder)} method for applying
 * a filter to a string buffer.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 *
 * @since 0.5
 *
 * @see FilterWriter
 */
public abstract class AbstractFilterWriter extends FilterWriter {
    /**
     * Stores full line read from the buffer of characters.
     */
    protected final StringBuilder buffer = new StringBuilder();

    /**
     * Create a new filtered writer for subclasses only.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    protected AbstractFilterWriter(final Writer out) {
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
        for (int i = offset; i < upper; i++) {
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
     * Apply a filter on a line given in parameter and return the resulting
     * filtered string.
     *
     * @param buffer A line to filter.
     * @return The line after applying the filter.
     */
    protected abstract String filterLine(final StringBuilder buffer);

    /**
     * Flush the buffer into the output.
     *
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        out.write(filterLine(buffer));
        buffer.setLength(0);
        super.flush();
    }

    /**
     * Close the output stream, and write all the remaining string stored in the
     * {@link #buffer} to the output.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        out.write(filterLine(buffer));
        buffer.setLength(0);
        super.close();
    }
}
