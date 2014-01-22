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

import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.PortrayalContext;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RestFull API for provider data rendering/portraying.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/portrayal")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class Portrayal {

    /**
     * @see LayerProviders#portray(PortrayalContext);
     */
    @POST
    @Path("/portray")
    public Response portray(final PortrayalContext context) {
        try {
            return Response.ok(LayerProviders.portray(context)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    @GET
    @Path("/portray")
    public Response portray2(@QueryParam("PROVIDER") final String providerId, @QueryParam("LAYERS") final String bandId, @QueryParam("BBOX") final String bbox,
                             @QueryParam("SRS") final String crs, @QueryParam("WIDTH") final int width, @QueryParam("HEIGHT") final int height) {
        try {
            return Response.ok(LayerProviders.portrayBand(providerId, bandId, crs, bbox, width, height)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }
}
