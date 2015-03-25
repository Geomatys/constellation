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

import org.constellation.ServiceDef.Specification;
import org.constellation.admin.ServiceBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.WebdavContext;
import org.constellation.dto.Details;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.WSEngine;
import org.geotoolkit.util.FileUtilities;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonUnmarshaller;

import javax.inject.Inject;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public final class OGCServicesRest {
    
    @Inject
    private ServiceRepository serviceRepository;
    
    @Inject
    private IServiceBusiness serviceBusiness;
    
    /**
     * Find and returns a i18n service {@link Instance}.
     *
     * @param serviceType The type of the service.
     * @param identifier the service identifier.
     * @param lang the language of current user
     *
     * 
     * @return an {@link Instance} instance
     */
    @GET
    @Path("{id}/{lang}")
    public Response getInstance(final @PathParam("spec") String serviceType, final @PathParam("id") String identifier, final @PathParam("lang") String lang) throws ConfigurationException {
        Instance instance = serviceBusiness.getI18nInstance(serviceType, identifier, lang);
        return ok(instance);
    }

    /**
     * Find and returns a service {@link Instance}.
     *
     * @param serviceType The type of the service.
     * @param id the service identifier
     *
     * @return an {@link Instance} instance
     */
    @GET
    @Path("{id}")
    public Response getInstance(final @PathParam("spec") String serviceType, final @PathParam("id") String id) throws ConfigurationException {
        return ok(getConfigurer(serviceType).getInstance(serviceType, id));
    }

    /**
     * Returns list of service {@link Instance}(s) related to the {@link OGCConfigurer}
     * implementation.
     *
     * @param serviceType The type of the service.
     * @return the {@link Instance} list
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @GET
    @Path("all")
    public Response getInstances(final @PathParam("spec") String serviceType) throws ConfigurationException {
        final OGCConfigurer configurer = getConfigurer(serviceType);
        final List<Instance> instances = new ArrayList<>();
        final Map<String, ServiceStatus> statusMap = serviceBusiness.getStatus(serviceType);
        for (final String key : statusMap.keySet()) {
            instances.add(configurer.getInstance(serviceType, key));
        }
        return ok(new InstanceReport(instances));
    }

    // TODO move elsewhere / rename
    @GET
    @Path("list")
    public Response listService() {
        final ServiceReport response = new ServiceReport(WSEngine.getRegisteredServices());
        return Response.ok(response).build();
    }
    
    /**
     * Creates a new service instance.
     *
     * @param domainId
     * @param spec      The type of the service.
     * @param metadata  The service metadata (can be null)
     *
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @PUT
    @Path("domain/{domainId}")
    public Response addInstance(@PathParam("domainId") int domainId, final @PathParam("spec") String spec, final Details metadata) throws ConfigurationException {
        
        Service service = serviceRepository.findByIdentifierAndType(metadata.getIdentifier(), spec);
        if(service != null) {
            return ok(AcknowlegementType.failure("Instance already created"));
        }
        
        serviceBusiness.create(spec, metadata.getIdentifier(), null, metadata, domainId);
        
        return created(AcknowlegementType.success(spec.toUpperCase() + " service \"" + metadata.getIdentifier() + "\" successfully created."));
    }

    /**
     * Starts a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier
     *
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/start")
    public Response start(final @PathParam("spec") String serviceType, final @PathParam("id") String id) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        serviceBusiness.start(serviceType, id);
        
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" successfully started."));
    }

    /**
     * Stops a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier
     *
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/stop")
    public Response stop(final @PathParam("spec") String serviceType, final @PathParam("id") String id) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        serviceBusiness.stop(serviceType, id);
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" successfully stopped."));
    }

    /**
     * Restarts a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier
     * @param stopFirst   Indicates if the service should be closed before trying to restart it
     *
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/restart")
    public Response restart(final @PathParam("spec") String serviceType, final @PathParam("id") String id, final SimpleValue stopFirst) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        serviceBusiness.restart(serviceType, id, stopFirst.getAsBoolean());
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" successfully restarted."));
    }

    /**
     * Renames a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The current service identifier.
     * @param newId       The new service identifier.
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/rename")
    public Response rename(final @PathParam("spec") String serviceType, final @PathParam("id") String id, final SimpleValue newId) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        serviceBusiness.rename(serviceType, id, newId.getValue());
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" successfully renamed."));
    }

    /**
     * Deletes a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier.
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @DELETE
    @Path("{id}")
    public Response delete(final @PathParam("spec") String serviceType, final @PathParam("id") String id) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        serviceBusiness.delete(serviceType, id);
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" successfully deleted."));
    }

    /**
     * Returns the configuration object of a service instance.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier.
     * 
     * @return a configuration {@link Object} (depending on implementation)
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @GET
    @Path("{id}/config")
    public Response getConfiguration(final @PathParam("spec") String serviceType, final @PathParam("id") String id) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        return ok(serviceBusiness.getConfiguration(serviceType, id));
    }

    /**
     * Updates a service instance configuration object.
     *
     * @param serviceType The type of the service.
     * @param id    the service identifier
     * @param configuration the service configuration
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("{id}/config")
    public Response setConfiguration(final @PathParam("spec") String serviceType, final @PathParam("id") String id, final InputStream configuration) throws ConfigurationException {
        try {
            final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final Object config = um.unmarshal(configuration);
            GenericDatabaseMarshallerPool.getInstance().recycle(um);
            
            final Details details = serviceBusiness.getInstanceDetails(serviceType, id, null);
            serviceBusiness.configure(serviceType, id, details, config);
        } catch (JAXBException ex) {
            throw new ConfigurationException("Error while unmarshalling configuration object.", ex);
        }
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" configuration successfully updated."));
        
    }

    /**
     * Updates a service instance configuration object.
     *
     * @param serviceType The type of the service.
     * @param id    the service identifier
     * @param configuration the service configuration
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}/config")
    public Response setConfigurationJson(final @PathParam("spec") String serviceType, final @PathParam("id") String id, final InputStream configuration) throws ConfigurationException {
        final Map<String, String> nSMap = new HashMap<>(0);
        nSMap.put("http://www.constellation.org/config", "constellation-config");
        final JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(nSMap).build();
        try {
            final JettisonJaxbContext cxtx = new JettisonJaxbContext(config, "org.constellation.configuration:" +
                    "org.constellation.generic.database:" +
                    "org.geotoolkit.ogc.xml.v110:" +
                    "org.apache.sis.internal.jaxb.geometry:" +
                    "org.geotoolkit.gml.xml.v311");
            final JettisonUnmarshaller jsonUnmarshaller = cxtx.createJsonUnmarshaller();

            final Class c;
            final String json = FileUtilities.getStringFromStream(configuration);
            if (json.startsWith("{\"automatic\"")) {
                c = Automatic.class;
            } else if (json.startsWith("{\"constellation-config.LayerContext\"")) {
                c = LayerContext.class;
            } else if (json.startsWith("{\"constellation-config.ProcessContext\"")) {
                c = ProcessContext.class;
            } else if (json.startsWith("{\"constellation-config.SOSConfiguration\"")) {
                c = SOSConfiguration.class;
            } else if (json.startsWith("{\"constellation-config.WebdavContext\"")) {
                c = WebdavContext.class;
            } else {
                return ok(AcknowlegementType.failure("Unknown configuration object given, unable to update service configuration"));
            }
            final Object configObj = jsonUnmarshaller.unmarshalFromJSON(new StringReader(json), c);
            final Details details = serviceBusiness.getInstanceDetails(serviceType, id, null);
            serviceBusiness.configure(serviceType, id, details, configObj);
        } catch (JAXBException | IOException e) {
            throw new ConfigurationException(e);
        }
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" configuration successfully updated."));
    }

    /**
     * Returns a service instance metadata.
     *
     * @param serviceType The type of the service.
     * @param id the service identifier
     * @return 
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @GET
    @Path("{id}/metadata/{lang}")
    public Response getDetails(final @PathParam("spec") String serviceType,
                               final @PathParam("id") String id,
                               final @PathParam("lang") String lang) throws ConfigurationException {
        return ok(serviceBusiness.getInstanceDetails(serviceType, id, lang));
    }

    /**
     * Updates a service instance metadata.
     *
     * @param serviceType The type of the service.
     * @param id          The service identifier
     * @param metadata    The service metadata
     *
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    @POST
    @Path("{id}/metadata")
    public Response setDetails(final @PathParam("spec") String serviceType, final @PathParam("id") String id, final Details metadata) throws ConfigurationException {
        serviceBusiness.ensureExistingInstance(serviceType, id);
        final Object config = serviceBusiness.getConfiguration(serviceType, id);
        serviceBusiness.configure(serviceType, id, metadata, config);
        return ok(AcknowlegementType.success(serviceType.toUpperCase() + " service \"" + id + "\" details successfully updated."));
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
