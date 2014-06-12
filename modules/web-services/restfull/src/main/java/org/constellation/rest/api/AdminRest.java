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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.ServiceDef.Specification;
import org.constellation.admin.ConfigurationBusiness;
import org.constellation.api.CommonConstants;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.configuration.ServiceReport;
import org.constellation.dto.Configuration;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
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
@Path("/1/admin")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class AdminRest {

    @Inject
    private ServiceRepository serviceRepository;
    @Inject
    private LayerRepository layerRepository;

    /**
     * service to return available service list
     *
     * @return
     */
    @GET
    @Path("/serviceType")
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
    @Path("/configurationLocation")
    public Response configurationPath() throws CstlServiceException {
        final String path = ConfigurationBusiness.getConfigPath();
        return Response.ok(new AcknowlegementType(true, path)).build();
    }

    /**
     * Reset configuration path
     * 
     * @param configuration
     *            contain new path
     * @return an {@link AcknowlegementType} on {@link Response} to know
     *         operation state
     * @throws CstlServiceException
     */
    @POST
    @Path("/configurationLocation")
    public Response configurationPath(final Configuration configuration) throws CstlServiceException {
        final String path = configuration.getPath();
        final boolean result = ConfigurationBusiness.setConfigPath(path);
        return Response.ok(new AcknowlegementType(result, path)).build();
    }

    /**
     *
     * @return the value of the constellation property
     * @throws CstlServiceException
     */
    @GET
    @Path("/property/{key}")
    public Response getKey(@PathParam("key") String key) throws CstlServiceException {
        return Response.ok(new SimpleValue(ConfigurationBusiness.getProperty(key))).build();
    }

    /**
     * Set a constellation property
     * 
     * @return an {@link AcknowlegementType} on {@link Response} to know
     *         operation state
     * @throws CstlServiceException
     */
    @POST
    @Path("/property/{key}")
    public Response setKey(@PathParam("key") String key, final SimpleValue value) throws CstlServiceException {
        ConfigurationBusiness.setProperty(key, value.getValue());
        return Response.ok(new AcknowlegementType(CommonConstants.SUCCESS, "the key have been set")).build();
    }

//    /**
//     *
//     * @return
//     */
//    @GET
//    @Path("/domain/{domainId}/instances")
//    public Response listInstances(@PathParam("domainId") int domainId, @Context HttpServletRequest httpServletRequest) {
//        final List<Instance> instances = new ArrayList<>();
//        final Set<String> services = WSEngine.getRegisteredServices().keySet();
//
//        Map<String, Set<String>> servicesByType = serviceRepository.getAccessiblesServicesByType(domainId,
//                httpServletRequest.getUserPrincipal().getName());
//
//        for (final String service : services) {
//            try {
//                final Specification spec = Specification.fromShortName(service);
//                Set<String> serviceIdentifiers = servicesByType.get(spec.name());
//
//                if (serviceIdentifiers != null) {
//                    final OGCConfigurer configurer = (OGCConfigurer) ServiceConfigurer.newInstance(spec);
//                    for (Instance instance : configurer.getInstances(spec.name())) {
//                        if (serviceIdentifiers.contains(instance.getIdentifier())) {
//                            instances.add(instance);
//                        }
//                    }
//
//                }
//            } catch (NotRunningServiceException ignore) {
//            }
//        }
//        return Response.ok(new InstanceReport(instances)).build();
//    }
    
    /**
  *
  * @return
  */
 @GET
 @Path("/domain/{domainId}/instances")
 public Response listInstances(@PathParam("domainId") int domainId, @Context HttpServletRequest httpServletRequest) {
     final List<Instance> instances = new ArrayList<>();
     List<Service> services = serviceRepository.findByDomain(domainId);
     for (Service service : services) {
	    Instance instance = new Instance();
	    instance.setId(service.getId());
	    instance.set_abstract("TODO");
	    instance.setIdentifier(service.getIdentifier());
	    int layersNumber = layerRepository.findByServiceId(service.getId()).size();
	    instance.setLayersNumber(layersNumber);
	    instance.setName(service.getIdentifier());
	    instance.setType(service.getType());
	    instance.setVersions(Arrays.asList(service.getVersions().split("|")));
	    instances.add(instance);
    }
     return Response.ok(new InstanceReport(instances)).build();
 }
}
