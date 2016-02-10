/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2013-2016 Geomatys.
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
package org.constellation.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.ServiceDef;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.WebdavContext;
import org.constellation.dto.Details;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.Automatic;

/**
 *
 */
public class ServicesAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    ServicesAPI(final ConstellationClient client) {
        this.client = client;
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
    
    /**
     * path : /1/OGC/{spec}/{id}/start<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.start<br>
     * <br>
     * Starts a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void start(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/start";
        client.post(path, MediaType.APPLICATION_XML_TYPE, "").ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}/{lang}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.getInstance<br>
     */
    public void getInstance(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/OGC/{spec}/{id}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.getInstance<br>
     * <br>
     * Queries a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link Instance} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public Instance getInstance(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        GenericType<JAXBElement<Instance>> planetType = new GenericType<JAXBElement<Instance>>() {
        };

        final String path = "api/1/OGC/" + serviceType + "/" + identifier;
        return client.getWebTarget().path(path).request().accept(MediaType.APPLICATION_XML_TYPE).get(planetType).getValue();
    }

    /**
     * path : /1/OGC/{spec}/{id}/stop<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.stop<br>
     * <br>
     * Stops a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void stop(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/stop";
        client.post(path, MediaType.APPLICATION_XML_TYPE, null).ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.OGCServicesRest.delete<br>
     * <br>
     * Deletes a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void delete(final ServiceDef.Specification serviceType, final String identifier) throws IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier;
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}/rename<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.rename<br>
     * <br>
     * Renames a service instance.
     *
     * @param serviceType   the service type
     * @param identifier    the service identifier
     * @param newIdentifier the new service identifier
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void rename(final ServiceDef.Specification serviceType, final String identifier, final String newIdentifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/rename";
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(newIdentifier)).ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}/config<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.getConfiguration<br>
     * <br>
     * Queries a service instance configuration.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return the instance configuration for this service.
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public Object getConfiguration(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/config";
        final Class classBinding;
        if (ServiceDef.Specification.CSW.equals(serviceType)) {
            classBinding = Automatic.class;
        } else if (ServiceDef.Specification.WMS.equals(serviceType) || ServiceDef.Specification.WMTS.equals(serviceType) ||
                   ServiceDef.Specification.WFS.equals(serviceType) || ServiceDef.Specification.WCS.equals(serviceType)) {
            classBinding = LayerContext.class;
        } else if (ServiceDef.Specification.WPS.equals(serviceType)) {
            classBinding = ProcessContext.class;
        } else if (ServiceDef.Specification.SOS.equals(serviceType)) {
            classBinding = SOSConfiguration.class;
        } else if (ServiceDef.Specification.WEBDAV.equals(serviceType)) {
            classBinding = WebdavContext.class;
        } else {
            throw new IOException("Invalid specification chosen to get instance configuration "+ serviceType);
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(classBinding);
    }

    /**
     * path : /1/OGC/{spec}/all<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.getInstances<br>
     * <br>
     * Queries the list of created services matching with the specified type
     * (even if not running).
     *
     * @param serviceType the service type
     * @return an {@link InstanceReport} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public InstanceReport getInstances(final ServiceDef.Specification serviceType) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);

        final String path = "OGC/" + serviceType + "/all";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(InstanceReport.class);
    }

    /**
     * path : /1/OGC/{spec}/{id}/restart<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.restart<br>
     * <br>
     * Restarts a service instance.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param stopFirst   the restart options
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void restart(final ServiceDef.Specification serviceType, final String identifier, final Boolean stopFirst) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/restart";
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(stopFirst)).ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}/config<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.setConfiguration<br>
     * <br>
     * Sets the service configuration..
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param config configuration object for the service
     *
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public void setConfiguration(final ServiceDef.Specification serviceType, final String identifier, final AbstractConfigurationObject config) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);
        ensureNonNull("config",  config);

        final String path = "OGC/" + serviceType + "/" + identifier + "/config";
        client.post(path, MediaType.APPLICATION_XML_TYPE, config).ensure2xxStatus();
    }

    /**
     * path : /1/OGC/{spec}/{id}/config<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.setConfigurationJson<br>
     */
    public void setConfigurationJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/OGC/{spec}/list<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.listService<br>
     */
    public void listService(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    public Map<String, List<String>> listAllServices() throws IOException {
        final String path = "OGC/whatever/list";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(ServiceReport.class).getAvailableServices();
    }

    /**
     * path : /1/OGC/{spec}/domain/{domainId}<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.OGCServicesRest.addInstance<br>
     * <br>
     * Create a new service instance.
     *
     * @param serviceType the service type
     * @param metadata    the service metadata
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void addInstance(final ServiceDef.Specification serviceType, final Details metadata) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("metadata",    metadata);

        final String domain = "0";// TODO
        final String path = "OGC/" + serviceType + "/domain/" + domain;
        client.put(path, MediaType.APPLICATION_XML_TYPE, metadata).ensure2xxStatus();
    }


    /**
     * path : /1/OGC/{spec}/{id}/metadata/{lang}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.OGCServicesRest.getDetails<br>
     * <br>
     * Queries a service instance metadata.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link org.constellation.dto.Details} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public Details getMetadata(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/metadata";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Details.class);
    }

    /**
     * path : /1/OGC/{spec}/{id}/metadata<br>
     * method : POST<br>
     * java : org.constellation.rest.api.OGCServicesRest.setDetails<br>
     * <br>
     * Updates a service instance metadata.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param metadata    the service metadata
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void setDetails(final ServiceDef.Specification serviceType, final String identifier, final Details metadata) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("metadata",    metadata);

        final String path = "OGC/" + serviceType + "/" + identifier + "/metadata";
        client.post(path, MediaType.APPLICATION_XML_TYPE, metadata).ensure2xxStatus();
    }



}
