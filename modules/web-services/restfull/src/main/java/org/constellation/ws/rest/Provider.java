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

package org.constellation.ws.rest;

import org.constellation.ws.rs.LayerProviderConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RestFul API for layer management/operations.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/provider")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class Provider {

    /**
     * @see LayerProviderConfiguration#getLayerDataInfo(String, String)
     */
    @GET
    @Path("{id}/{layerName}/info")
    public Response layerDataInfo(final @PathParam("id") String id, final @PathParam("layerName") String layerName) {
        final Object result = LayerProviderConfiguration.getLayerDataInfo(id, layerName);
        return Response.ok(result).build();
    }
}
