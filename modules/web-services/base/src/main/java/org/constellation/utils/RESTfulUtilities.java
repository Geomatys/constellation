/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.utils;

import org.apache.sis.util.Static;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Utility class for RESTful services.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class RESTfulUtilities extends Static {

    // Success.
    public static Response ok(final Object entity) {
        return Response.status(200).entity(entity).build();
    }
    public static Response ok(final String message) {
        return Response.status(200).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    public static Response created(final String message) {
        return Response.status(201).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    public static Response noContent() {
        return Response.status(204).build();
    }

    // Errors.
    public static Response badRequest(final String message) {
        return Response.status(400).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    public static Response notFound(final String message) {
        return Response.status(404).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    public static Response internalError(final String message) {
        return Response.status(500).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
