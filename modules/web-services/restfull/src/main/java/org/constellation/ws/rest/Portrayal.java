/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.PortrayalContext;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;

/**
 * RestFull API for provider data rendering/portraying.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/portrayal")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces("image/png")
public final class Portrayal {

    /**
     * @see LayerProviders#portray(String, String, String, String, int, int, String, String)
     */
    @GET
    @Path("/portray")
    public Response portray(@QueryParam("PROVIDER") final String providerId,
                            @QueryParam("LAYERS") final String dataName,
                            @QueryParam("BBOX") final String bbox,
                            @QueryParam("CRS") final String crs,
                            @QueryParam("WIDTH") final int width,
                            @QueryParam("HEIGHT") final int height,
                            @QueryParam("SLD_BODY") final String sldBody,
                            @QueryParam("SLD_VERSION") final String sldVersion) {
        try {
            return Response.ok(LayerProviders.portray(providerId, dataName, crs, bbox, width, height, sldBody, sldVersion)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }
    
    
    
    
    /**
     * @see LayerProviders#portray(String, String, String, String, int, int, String)
     */
    @GET
    @Path("/portray/style")
    public Response portray(@QueryParam("PROVIDER") final String providerId,
                            @QueryParam("LAYERS") final String dataName,
                            @QueryParam("BBOX") final String bbox,
                            @QueryParam("CRS") final String crs,
                            @QueryParam("WIDTH") final int width,
                            @QueryParam("HEIGHT") final int height,
                            @QueryParam("SLD_BODY") final String sldBody,
                            @QueryParam("SLD_VERSION") final String sldVersion,
                            @QueryParam("SLDPROVIDER") final String sldProvider,
                            @QueryParam("SLDID") final String styleId) {
                           
        try {
            return Response.ok(LayerProviders.portray(providerId, dataName, crs, bbox, width, height, sldBody, sldVersion,sldProvider,styleId )).build();
        } catch (CstlServiceException | TargetNotFoundException | JAXBException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage()), MediaType.APPLICATION_XML).build();
        }
    }


    /**
     * @see LayerProviders#portray(PortrayalContext)
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
}
