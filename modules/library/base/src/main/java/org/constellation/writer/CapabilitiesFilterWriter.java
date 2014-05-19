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
    public CapabilitiesFilterWriter(final Writer out) {
        super(out);
    }

    /**
     * Create a new filtered outputStream for WMS 1.1.1 Capabilities.
     *
     * @param out A Output stream object to provide the underlying stream.
     * @param enc The character encoding.
     * @throws NullPointerException if <code>out</code> is <code>null</code>
     */
    public CapabilitiesFilterWriter(final OutputStream out, final String enc) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(out, enc));
    }

    /**
     * In general, returns the line unchanged.
     * If the line is the root tag WMT_MS_Capabilities, then it just returns the root tag
     * with only the version number. Consequently it will remove all namespace definitions
     * if there are some.
     * If the line is a OnlineResource tag, then add the xml namespace xlink URL.
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
