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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
public final class CapabilitiesFilterWriter extends AbstractFilterWriter {
    /**
     * Create a new filtered writer for WMS 1.1.1 Capabilities.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public CapabilitiesFilterWriter(Writer out) {
        super(out);
    }

    /**
     * Create a new filtered outputStream for WMS 1.1.1 Capabilities.
     *
     * @param out A Output stream object to provide the underlying stream.
     * @param enc The character encoding.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public CapabilitiesFilterWriter(OutputStream out, String enc) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(out, enc));
    }

    /**
     * In general, returns the line unchanged.
     * If the line is the root tag WMT_MS_Capabilities, then it justs returns the root tag
     * with only the version number. Consequently it will remove all namespace definitions
     * if there are some.
     * If the line is a OnlineResource tag, then add the xml namespace xlink url.
     *
     * @param buffer Contains a whole line.
     */
    @Override
    protected String filterLine(final StringBuilder buffer) {
        String line = buffer.toString();
        if (line.trim().startsWith("<WMT_MS_Capabilities")) {
            line = "<WMT_MS_Capabilities version=\"1.1.1\">";
        }
        if (line.trim().startsWith("<OnlineResource")) {
            line = line.replaceAll("/>", " xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>");
        }
        return line;
    }
}
