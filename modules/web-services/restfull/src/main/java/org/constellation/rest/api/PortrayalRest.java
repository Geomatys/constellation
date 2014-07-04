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

package org.constellation.rest.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.PortrayalContext;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;

import java.util.logging.Level;
import java.util.logging.Logger;

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
public final class PortrayalRest {
    
    @Inject
    LayerProviders layerProviders;
    
    private static final Logger LOGGER = Logging.getLogger(PortrayalRest.class);

    /**
     * @see LayerProviders#portray(String, String, String, String, int, int, String, String, String)
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
                            @QueryParam("SLD_VERSION") final String sldVersion,
                            @QueryParam("CQLFILTER") final String filter) {
        try {
            return Response.ok(LayerProviders.portray(providerId, dataName, crs, bbox, width, height, sldBody, sldVersion, filter)).build();
        } catch (CstlServiceException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }
    
    
    
    
    /**
     * @see LayerProviders#portray(String, String, String, String, int, int, String, String, String, String)
     */
    @GET
    @Path("/portray/style")
    public Response portrayStyle(@QueryParam("PROVIDER") final String providerId,
                            @QueryParam("LAYERS") final String dataName,
                            @QueryParam("BBOX") final String bbox,
                            @QueryParam("CRS") final String crs,
                            @QueryParam("WIDTH") final int width,
                            @QueryParam("HEIGHT") final int height,
                            @QueryParam("SLD_VERSION") final String sldVersion,
                            @QueryParam("SLDPROVIDER") final String sldProvider,
                            @QueryParam("SLDID") final String styleId,
                            @QueryParam("CQLFILTER") final String filter) {
                           
        try {
            return Response.ok(layerProviders.portray(providerId, dataName, crs, bbox, width, height, sldVersion, sldProvider, styleId, filter)).build();
        } catch (CstlServiceException | TargetNotFoundException | JAXBException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
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
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }
}
