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

import org.constellation.admin.MapContextBusiness;
import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.Mapcontext;
import org.constellation.engine.register.MapcontextStyledLayer;
import org.constellation.engine.register.repository.MapContextRepository;
import org.constellation.provider.Providers;
import org.opengis.util.FactoryException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;


/**
 * Map context REST API.
 *
 * @author Cédric Briançon (Geomatys)
 */
@Path("/1/context")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class MapContextRest {
    @Inject
    private MapContextBusiness contextBusiness;

    @Inject
    private MapContextRepository contextRepository;

    @GET
    @Path("/list")
    public Response findAll() {
        return Response.ok(contextRepository.findAll()).build();
    }

    @GET
    @Path("/list/layers")
    public Response findAllMapContextLayers() {
        return Response.ok(contextBusiness.findAllMapContextLayers()).build();
    }

    @PUT
    @Path("/")
    @Transactional
    public Response create(final Mapcontext mapContext) {
        final Mapcontext mapContextCreated = contextRepository.create(mapContext);
        return Response.ok(mapContextCreated).build();
    }

    @POST
    @Path("/")
    @Transactional
    public Response update(final Mapcontext mapContext) {
        contextRepository.update(mapContext);
        return Response.ok(mapContext).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") final int contextId) {
        contextRepository.delete(contextId);
        return Response.status(204).build();
    }

    @POST
    @Path("/layers/{id}")
    public Response setMapItems(@PathParam("id") final int contextId, final List<MapcontextStyledLayer> layers) {
        contextBusiness.setMapItems(contextId, layers);
        return Response.status(201).build();
    }

    @GET
    @Path("/{id}/extent")
    public Response getContextExtents(@PathParam("id") final int contextId) {
        final ParameterValues values;
        try {
            values = contextBusiness.getExtent(contextId);
        } catch (FactoryException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract envelope for context "+contextId+". "+ex.getMessage()).status(500).build();
        }
        if (values == null) {
            return Response.status(500).build();
        }
        return Response.ok(values).build();
    }
}
