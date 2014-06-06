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

package org.constellation.admin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.LayerList;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.ServiceReport;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Service;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.Automatic;

/**
 * Constellation RESTful API for services management/configuration.
 *
 * @author Bernard Fabien (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ServicesAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     * Creates a {@link ServicesAPI} instance.
     *
     * @param client the client to use
     */
    ServicesAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * Queries a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link Instance} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public Instance getInstance(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        GenericType<JAXBElement<Instance>> planetType = new GenericType<JAXBElement<Instance>>() {
        };

        final String path = "api/1/OGC/" + serviceType + "/" + identifier;
        return client.target().path(path).request().accept(MediaType.APPLICATION_XML_TYPE).get(planetType).getValue();
    }

    /**
     * Queries the list of created services matching with the specified type
     * (even if not running).
     *
     * @param serviceType the service type
     * @return an {@link InstanceReport} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public InstanceReport getInstances(final Specification serviceType) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);

        final String path = "OGC/" + serviceType + "/all";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(InstanceReport.class);
    }

    public Map<String, List<String>> getAvailableService() throws IOException {
        final String path = "OGC/whatever/list";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(ServiceReport.class).getAvailableServices();
    }
    
    /**
     * Create a new service instance.
     *
     * @param serviceType the service type
     * @param metadata    the service metadata
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void newInstance(final Specification serviceType, final Service metadata) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("metadata",    metadata);

        final String path = "OGC/" + serviceType;
        client.put(path, MediaType.APPLICATION_XML_TYPE, metadata).ensure2xxStatus();
    }

    /**
     * Queries a service instance configuration.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return the instance configuration for this service.
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public Object getInstanceConfiguration(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/config";
        final Class classBinding;
        if (Specification.CSW.equals(serviceType)) {
            classBinding = Automatic.class;
        } else if (Specification.WMS.equals(serviceType) || Specification.WMTS.equals(serviceType) ||
                   Specification.WFS.equals(serviceType) || Specification.WCS.equals(serviceType)) {
            classBinding = LayerContext.class;
        } else if (Specification.WPS.equals(serviceType)) {
            classBinding = ProcessContext.class;
        } else if (Specification.SOS.equals(serviceType)) {
            classBinding = SOSConfiguration.class;
        } else {
            throw new IOException("Invalid specification chosen to get instance configuration "+ serviceType);
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(classBinding);
    }

    /**
     * Sets the service configuration..
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param config configuration object for the service
     *
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public void setInstanceConfiguration(final Specification serviceType, final String identifier, final AbstractConfigurationObject config) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);
        ensureNonNull("config",  config);

        final String path = "OGC/" + serviceType + "/" + identifier + "/config";
        client.post(path, MediaType.APPLICATION_XML_TYPE, config).ensure2xxStatus();
    }

    /**
     * Queries a service instance metadata.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link Service} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public Service getMetadata(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/metadata";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Service.class);
    }

    /**
     * Updates a service instance metadata.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param metadata    the service metadata
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void setMetadata(final Specification serviceType, final String identifier, final Service metadata) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("metadata",    metadata);

        final String path = "OGC/" + serviceType + "/" + identifier + "/metadata";
        client.post(path, MediaType.APPLICATION_XML_TYPE, metadata).ensure2xxStatus();
    }

    /**
     * Starts a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void start(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/start";
        client.post(path, MediaType.APPLICATION_XML_TYPE, "").ensure2xxStatus();
    }

    /**
     * Stops a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void stop(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/stop";
        client.post(path, MediaType.APPLICATION_XML_TYPE, null).ensure2xxStatus();
    }

    /**
     * Restarts a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param stopFirst   the restart options
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void restart(final Specification serviceType, final String identifier, final Boolean stopFirst) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/restart";
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(stopFirst)).ensure2xxStatus();
    }

    /**
     * Renames a service instance.
     *
     * @param serviceType   the service type
     * @param identifier    the service identifier
     * @param newIdentifier the new service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void rename(final Specification serviceType, final String identifier, final String newIdentifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/rename";
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(newIdentifier)).ensure2xxStatus();
    }

    /**
     * Deletes a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void delete(final Specification serviceType, final String identifier) throws IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier;
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }

    /**
     * Queries the layer list of a "map" service instance.
     * <p>
     * Only for "map" services: WMS, WMTS, WCS, WFS.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link LayerList} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public LayerList getLayers(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "MAP/" + serviceType + "/" + identifier + "/layer/all";
        return (LayerList) client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(LayerList.class);
    }

    /**
     * Adds a new layer to a "map" service instance.
     * <p>
     * Only for "map" services: WMS, WMTS, WCS, WFS.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param layer the layer to be added
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void addLayer(final Specification serviceType, final String identifier, final AddLayer layer) throws HttpResponseException, IOException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("layer", layer);

        final String path = "MAP/" + serviceType + "/" + identifier + "/layer";
        client.put(path, MediaType.APPLICATION_XML_TYPE, layer).ensure2xxStatus();
    }

    /**
     * delete service layer
     * @param serviceId service identifier
     * @param layerId data layer name
     * @param layerNamespace
     * @param spec service specification
     * @throws IOException
     */
    public void deleteLayer(final String serviceId, final String layerId, final String layerNamespace, final String spec) throws IOException {
        client.delete("MAP/" + spec + "/" + serviceId+"/"+layerId, MediaType.APPLICATION_XML_TYPE, "layernamespace", layerNamespace).ensure2xxStatus();
    }
    
    /**
     * Return a complete URL for the specified service (wms, wfs, csw,...) and
     * instance identifier.
     *
     * @param service The service name (wms, wfs, csw,...).
     * @param instanceId The instance identifier.
     * @return A complete URL for the specified service.
     */
    public String getInstanceURL(final String service, final String instanceId) {
        return client.getUrl() + service.toLowerCase() + '/' + instanceId;
    }

}
