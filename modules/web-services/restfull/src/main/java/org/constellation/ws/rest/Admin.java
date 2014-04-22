/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013 - 2014, Geomatys
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.ServiceDef.Specification;
import org.constellation.api.CommonConstants;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.constellation.dto.Configuration;
import org.constellation.dto.SimpleValue;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;

/**
 * Restfull main configuration service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/admin/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class Admin {

    /**
     * service to return available service list
     *
     * @return
     */
    @GET
    @Path("serviceType")
    public Response serviceType() {
        final ServiceReport response = new ServiceReport(WSEngine.getRegisteredServices());
        return Response.ok(response).build();
    }

    /**
     *
     * @return configuration path on a {@link Response}
     * @throws CstlServiceException
     */
    @GET
    @Path("configurationLocation")
    public Response configurationPath() throws CstlServiceException {
        return Response.ok(ConfigurationUtilities.getConfigPath()).build();
    }

    /**
     * Reset configuration path
     * @param configuration contain new path
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     * @throws CstlServiceException
     */
    @POST
    @Path("configurationLocation")
    public Response configurationPath(final Configuration configuration) throws CstlServiceException {
        return Response.ok(ConfigurationUtilities.setConfigPath(configuration.getPath())).build();
    }
    
    /**
     *
     * @return the value of the constellation property
     * @throws CstlServiceException
     */
    @GET
    @Path("property/{key}")
    public Response getKey(@PathParam("key") String key) throws CstlServiceException {
        return Response.ok(new SimpleValue(ConfigurationUtilities.getProperty(key))).build();
    }

    /**
     * Set a constellation property
     * 
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     * @throws CstlServiceException
     */
    @POST
    @Path("property/{key}")
    public Response setKey(@PathParam("key") String key, final SimpleValue value) throws CstlServiceException {
        ConfigurationUtilities.setProperty(key, value.getValue());
        return Response.ok(new AcknowlegementType(CommonConstants.SUCCESS, "the key have been set")).build();
    }

    /**
     *
     * @return
     */
    @GET
    @Path("instances")
    public Response listInstances(){
        final List<Instance> instances = new ArrayList<>();
        final Set<String> services = WSEngine.getRegisteredServices().keySet();
        for (final String service : services) {
            try {
                final Specification spec = Specification.fromShortName(service);
                final OGCConfigurer configurer = (OGCConfigurer) ServiceConfigurer.newInstance(spec);
                instances.addAll(configurer.getInstances());
            } catch (NotRunningServiceException ignore) {
            }
        }
        return Response.ok(new InstanceReport(instances)).build();
    }
}
