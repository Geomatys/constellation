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
 * Filter for WMS 1.1.1 Exception files, which removes the namespaces declaration
 * in the root exception tag.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
public final class ExceptionFilterWriter extends AbstractFilterWriter {
    /**
     * Create a new filtered writer for WMS 1.1.1 Exception.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public ExceptionFilterWriter(Writer out) {
        super(out);
    }

    /**
     * Create a new filtered writer for WMS 1.1.1 Exception.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public ExceptionFilterWriter(OutputStream out, String enc) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(out, enc));
    }

    /**
     * Remove the namespaces declaration from the ServiceExceptionReport tag.
     *
     * @param buffer A line to filter.
     */
    @Override
    protected String filterLine(final StringBuilder buffer) {
        String line = buffer.toString();
        if (line.trim().startsWith("<ServiceExceptionReport") || line.trim().startsWith("<ogc:ServiceExceptionReport")) {
            line = "<ServiceExceptionReport version=\"1.1.1\">";
        }
        line = line.replace("ogc:", "");
        return line;
    }

}
