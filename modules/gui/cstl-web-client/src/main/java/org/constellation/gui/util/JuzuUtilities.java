/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.util;

import juzu.Response;
import juzu.impl.request.Request;
import juzu.request.RequestParameter;
import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class JuzuUtilities extends Static {

    /**
     * Use for debugging purpose.
     */
    private static final Logger LOGGER = Logging.getLogger(JuzuUtilities.class);

    /**
     * Gets a request parameters {@link String} value.
     * <p>
     * Also reverts the URL encoding for parameter value like {@code "%20"}...
     *
     * @param name the parameter name
     * @return the parameter value or {@code null}
     */
    public static String getRequestParameter(final String name) {
        ensureNonNull("name", name);
        final RequestParameter parameter = Request.getCurrent().getParameters().get(name);
        if (parameter != null) {
            try {
                return URLDecoder.decode(parameter.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    public static Response success() {
        return Response.ok("{\"status\":\"success\"}").withMimeType("application/json");
    }

    public static Response error() {
        return Response.ok("{\"status\":\"fail\"}").withMimeType("application/json");
    }
}
