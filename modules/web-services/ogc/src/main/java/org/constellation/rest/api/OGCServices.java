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

package org.constellation.rest.api;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.dto.Service;
import org.constellation.dto.SimpleValue;
import org.constellation.ogc.configuration.OGCConfigurer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.constellation.utils.RESTfulUtilities.created;
import static org.constellation.utils.RESTfulUtilities.ok;

/**
 * RESTful API for generic OGC services configuration.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/OGC/{spec}")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class OGCServices {

    /**
     * @see OGCConfigurer#getInstance(String)
     */
    @GET
    @Path("{id}")
    public Response getInstance(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer(spec).getInstance(id));
    }

    /**
     * @see OGCConfigurer#getInstances()
     */
    @GET
    @Path("all")
    public Response getInstances(final @PathParam("spec") String spec) throws Exception {
        return ok(new InstanceReport(getConfigurer(spec).getInstances()));
    }

    /**
     * @see OGCConfigurer#createInstance(String, Service, Object)
     */
    @PUT
    @Path("/")
    public Response addInstance(final @PathParam("spec") String spec, final Service metadata) throws Exception {
        getConfigurer(spec).createInstance(metadata.getIdentifier(), metadata, null);
        return created(AcknowlegementType.success(spec.toUpperCase() + " service \"" + metadata.getIdentifier() + "\" successfully created."));
    }

    /**
     * @see OGCConfigurer#startInstance(String)
     */
    @POST
    @Path("{id}/start")
    public Response start(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        getConfigurer(spec).startInstance(id);
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully started."));
    }

    /**
     * @see OGCConfigurer#stopInstance(String)
     */
    @POST
    @Path("{id}/stop")
    public Response stop(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        getConfigurer(spec).stopInstance(id);
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully stopped."));
    }

    /**
     * @see OGCConfigurer#restartInstance(String, boolean)
     */
    @POST
    @Path("{id}/restart")
    public Response restart(final @PathParam("spec") String spec, final @PathParam("id") String id, final SimpleValue stopFirst) throws Exception {
        getConfigurer(spec).restartInstance(id, stopFirst.getAsBoolean());
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully restarted."));
    }

    /**
     * @see OGCConfigurer#renameInstance(String, String)
     */
    @POST
    @Path("{id}/rename")
    public Response rename(final @PathParam("spec") String spec, final @PathParam("id") String id, final SimpleValue newId) throws Exception {
        getConfigurer(spec).renameInstance(id, newId.getValue());
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully renamed."));
    }

    /**
     * @see OGCConfigurer#deleteInstance(String)
     */
    @DELETE
    @Path("/")
    public Response delete(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        getConfigurer(spec).deleteInstance(id);
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" successfully deleted."));
    }

    /**
     * @see OGCConfigurer#getInstanceConfiguration(String)
     */
    @GET
    @Path("{id}/config")
    public Response getConfiguration(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer(spec).getInstanceConfiguration(id));
    }

    /**
     * @see OGCConfigurer#setInstanceConfiguration(String, Object)
     */
    @POST
    @Path("{id}/config")
    public Response setConfiguration(final @PathParam("spec") String spec, final @PathParam("id") String id, final Object config) throws Exception {
        getConfigurer(spec).setInstanceConfiguration(id, config);
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" configuration successfully updated."));
    }

    /**
     * @see OGCConfigurer#getInstanceMetadata(String)
     */
    @GET
    @Path("{id}/metadata")
    public Response getMetadata(final @PathParam("spec") String spec, final @PathParam("id") String id) throws Exception {
        return ok(getConfigurer(spec).getInstanceMetadata(id));
    }

    /**
     * @see OGCConfigurer#setInstanceMetadata(String, Service)
     */
    @POST
    @Path("{id}/metadata")
    public Response setMetadata(final @PathParam("spec") String spec, final @PathParam("id") String id, final Service metadata) throws Exception {
        getConfigurer(spec).setInstanceMetadata(id, metadata);
        return ok(AcknowlegementType.success(spec.toUpperCase() + " service \"" + id + "\" metadata successfully updated."));
    }

    /**
     * Returns the {@link OGCConfigurer} instance from its {@link Specification}.
     *
     * @throws NotRunningServiceException if the service is not activated or if an error
     * occurred during its startup
     */
    private static OGCConfigurer getConfigurer(final String specification) throws NotRunningServiceException {
        final Specification spec = Specification.fromShortName(specification);
        if (!spec.supported()) {
            throw new IllegalArgumentException(specification + " is not a valid OGC service.");
        }
        return (OGCConfigurer) ServiceConfigurer.newInstance(spec);
    }
}
