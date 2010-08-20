/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.query.wms;

import org.constellation.query.QueryRequest;
import org.constellation.ws.MimeType;

import org.geotoolkit.lang.Immutable;
import org.geotoolkit.util.Version;


/**
 * Representation of a {@code WMS GetCapabilities} request, with its parameters.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
@Immutable
public final class GetCapabilities extends WMSQuery {
    /**
     * The output format for this request.
     */
    private final String format;

    public GetCapabilities(final Version version) {
        this(version, null);
    }

    public GetCapabilities(final Version version, final String format) {
        super(version, null);
        this.format = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExceptionFormat() {
        return MimeType.APP_SE_XML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRequest getRequest() {
        return GET_CAPABILITIES;
    }

    public String getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final StringBuilder kvp = new StringBuilder();
        //Obligatory Parameters
        kvp.append(KEY_REQUEST).append('=').append(GETCAPABILITIES).append('&')
           .append(KEY_SERVICE).append('=').append(getService());

        //Optional Parameters
        final Version version = getVersion();
        if (version != null) {
            kvp.append('&').append(KEY_VERSION).append('=').append(version);
        }
        if (format != null) {
            kvp.append('&').append(KEY_FORMAT).append('=').append(format);
        }
        return kvp.toString();
    }
}
