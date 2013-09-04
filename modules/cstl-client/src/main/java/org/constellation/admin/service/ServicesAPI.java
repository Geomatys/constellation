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

package org.constellation.admin.service;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Service;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Constellation RESTful API for services management/configuration.
 *
 * @author Bernard Fabien (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class ServicesAPI {

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

        final String path = "OGC/" + serviceType + "/" + identifier;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Instance.class);
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
    public void delete(final Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "OGC/" + serviceType + "/" + identifier + "/delete";
        client.post(path, MediaType.APPLICATION_XML_TYPE, null).ensure2xxStatus();
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

        final String path = "map/" + serviceType + "/" + identifier + "/layer/all";
        return (LayerList) client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(GenericDatabaseMarshallerPool.getInstance());
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

        final String path = "map/" + serviceType + "/" + identifier + "/layer";
        client.put(path, MediaType.APPLICATION_XML_TYPE, layer).ensure2xxStatus();
    }
}
