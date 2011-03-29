/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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


package org.constellation.ws.rs.provider;

import org.geotoolkit.ows.xml.ExceptionResponse;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SchemaLocatedExceptionResponse implements ExceptionResponse {

    private final ExceptionResponse response;

    private final String schemaLocation;

    public SchemaLocatedExceptionResponse(final ExceptionResponse response, final String schemaLocation) {
        this.response       = response;
        this.schemaLocation = schemaLocation;
    }

    /**
     * @return the response
     */
    public ExceptionResponse getResponse() {
        return response;
    }

    /**
     * @return the schemaLocation
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

}
