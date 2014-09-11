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

import org.constellation.admin.ConfigurationBusiness;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.dto.ServiceLayersDTO;
import org.constellation.api.CommonConstants;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerSummary;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.Configuration;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private IServiceBusiness serviceBusiness;
    @Inject
    private LayerRepository layerRepository;

    @Inject
    private ILayerBusiness layerBusiness;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private org.constellation.security.SecurityManager securityManager;
    
    @Inject
    private IConfigurationBusiness configurationBusiness;

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
        return Response.ok(new SimpleValue(configurationBusiness.getProperty(key))).build();
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
        configurationBusiness.setProperty(key, value.getValue());
        return Response.ok(new AcknowlegementType(CommonConstants.SUCCESS, "the key have been set")).build();
    }



    @GET
    @Path("/domain/{domainId}/instances")
    public Response listInstances(@PathParam("domainId") int domainId) throws ConfigurationException {
        return listInstances(domainId,null);
    }

    @GET
    @Path("/domain/{domainId}/instances/{lang}")
    public Response listInstances(@PathParam("domainId") int domainId, @PathParam("lang") String lang) throws ConfigurationException {
        final List<Instance> instances = new ArrayList<>();
        final List<ServiceDTO> services = serviceBusiness.getAllServicesByDomainId(domainId, lang);
        for (ServiceDTO service : services) {
            final Instance instance = convertToInstance(service);
            instances.add(instance);
        }
        return Response.ok(new InstanceReport(instances)).build();
    }

    @GET
    @Path("/domain/{domainId}/instances/{lang}/{type}")
    public Response listInstancesByType(@PathParam("domainId") int domainId, @PathParam("lang") String lang, @PathParam("type") String type) throws ConfigurationException {
        final List<Instance> instances = new ArrayList<>();
        final List<ServiceDTO> services = serviceBusiness.getAllServicesByDomainIdAndType(domainId, lang, type);
        for (ServiceDTO service : services) {
            final Instance instance = convertToInstance(service);
            instances.add(instance);
        }
        return Response.ok(new InstanceReport(instances)).build();
    }

    @GET
    @Path("/domain/{domainId}/service/layers/{lang}")
    public Response listServiceLayers(@PathParam("domainId") int domainId, @PathParam("lang") String lang) throws ConfigurationException {
        final List<ServiceLayersDTO> serviceLayers = new ArrayList<>();
        final List<ServiceDTO> services = serviceBusiness.getAllServicesByDomainIdAndType(domainId, lang, "wms");
        for (final ServiceDTO service : services) {
            final List<Layer> layers = layerBusiness.getLayers("wms", service.getIdentifier(), securityManager.getCurrentUserLogin());
            final List<LayerSummary> layerSummaries = new ArrayList<>();
            for (final Layer lay : layers) {
                final DataBrief db = dataBusiness.getDataBrief(lay.getName(), lay.getProviderID());
                final LayerSummary sum = new LayerSummary(lay, db);
                layerSummaries.add(sum);
            }
            final ServiceLayersDTO servLay = new ServiceLayersDTO(service, layerSummaries);
            serviceLayers.add(servLay);
        }
        return Response.ok(serviceLayers).build();
    }

    private Instance convertToInstance(final ServiceDTO service) {
        final Instance instance = new Instance();
        instance.setId(service.getId());
        instance.set_abstract(service.getDescription());
        instance.setIdentifier(service.getIdentifier());
        int layersNumber = layerRepository.findByServiceId(service.getId()).size();
        instance.setLayersNumber(layersNumber);
        instance.setName(service.getTitle());
        instance.setType(service.getType());
        instance.setVersions(Arrays.asList(service.getVersions().split("µ")));
        instance.setStatus(ServiceStatus.valueOf(service.getStatus()));
        return instance;
    }
}
