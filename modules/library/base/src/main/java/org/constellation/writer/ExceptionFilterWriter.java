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
    public ExceptionFilterWriter(final Writer out) {
        super(out);
    }

    /**
     * Create a new filtered writer for WMS 1.1.1 Exception.
     *
     * @param out  a Writer object to provide the underlying stream.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public ExceptionFilterWriter(final OutputStream out, final String enc) throws UnsupportedEncodingException {
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
