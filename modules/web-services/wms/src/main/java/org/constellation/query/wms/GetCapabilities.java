/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
import org.constellation.ws.ServiceVersion;


/**
 * Representation of a {@code WMS GetCapabilities} request, with its parameters.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
public class GetCapabilities extends WMSQuery {
    /**
     * The output format for this request.
     */
    private final String format;

    /**
     * 
     */
    public GetCapabilities(final ServiceVersion version) {
        this(version, null);
    }

    public GetCapabilities(final ServiceVersion version, final String format) {
        super(version);
        this.format = format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExceptionFormat() {
        return "application/vnd.ogc.se_xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRequest getRequest() {
        return WMSQueryRequest.GET_CAPABILITIES;
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
        kvp            .append(KEY_REQUEST).append('=').append(GETCAPABILITIES)
           .append('&').append(KEY_SERVICE).append('=').append(getService().key);

        //Optional Parameters
        if (version != null) {
            kvp.append('&').append(KEY_VERSION).append('=').append(version);
        }
        if (format != null) {
            kvp.append('&').append(KEY_FORMAT).append('=').append(format);
        }
        return kvp.toString();
    }
}
