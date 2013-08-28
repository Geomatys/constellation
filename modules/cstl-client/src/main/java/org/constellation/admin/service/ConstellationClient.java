/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Restart;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensureStrictlyPositive;

/**
 * @author Bernard Fabien (Geomatys).
 * @since 0.9
 */
public final class ConstellationClient {

    /**
     * Jersey client.
     */
    private final Client client;

    /**
     * Constellation server URL.
     */
    private final String url;

    /**
     * Constellation configuration API version.
     */
    private final String version;

    /**
     * API methods related to services administration.
     */
    public final Services services;

    /**
     * API methods related to providers administration.
     */
    public final Providers providers;


    /**
     * Creates a new client instance ready to communicate with the Constellation server.
     * <p>
     * Use automatically the latest version.
     *
     * @param url the constellation server root url
     */
    public ConstellationClient(final String url) {
        this(url, "1");
    }

    /**
     * Creates a new client instance ready to communicate with the Constellation server.
     *
     * @param url     the constellation server root url
     * @param version the constellation configuration API version
     */
    public ConstellationClient(final String url, final String version) {
        ensureNonNull("url", url);
        ensureNonNull("version", version);

        this.url        = url.endsWith("/") ? url : url + "/";
        this.version    = version;
        this.services   = new Services();
        this.providers  = new Providers();

        // Initialize Jerzey client.
        this.client = Client.create();
        connectTimeout(5000);
        readTimeout(20000);

    }

    /**
     * Authenticates an user before trying to communicate with the Constellation server.
     *
     * @param login    the user login
     * @param password the user password
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient auth(final String login, final String password) {
        ensureNonNull("login",    login);
        ensureNonNull("password", password);
        this.client.addFilter(new HTTPBasicAuthFilter(login, password));
        return this;
    }

    /**
     * Configures the Jersey {@link Client} read timeout for HTTP communication.
     *
     * @param timeout the timeout value (in ms)
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient readTimeout(final int timeout) {
        ensureStrictlyPositive("timeout", timeout);
        this.client.setReadTimeout(timeout);
        return this;
    }

    /**
     * Configures the Jersey {@link Client} connection timeout for HTTP communication.
     *
     * @param timeout the timeout value (in ms)
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient connectTimeout(final int timeout) {
        ensureStrictlyPositive("timeout", timeout);
        this.client.setConnectTimeout(timeout);
        return this;
    }

    /**
     * API methods related to services actions/administration.
     */
    public final class Services {

        /**
         * Create a new service instance with the specified metadata on the Constellation
         * server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param metadata    the service metadata
         * @return an {@link AcknowlegementType} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType newInstance(final Specification serviceType, final Service metadata) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("metadata",    metadata);
            return post(serviceType + "/create", MediaType.APPLICATION_XML_TYPE, metadata).getEntity(AcknowlegementType.class);
        }

        /**
         * Queries a service instance from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return a {@link Instance} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public Instance getInstance(final Specification serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return get(serviceType + "/" + identifier + "/instance", MediaType.APPLICATION_XML_TYPE).getEntity(Instance.class);
        }

        /**
         * Queries the list of created services matching with the specified type (even if
         * not running) from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @return an {@link InstanceReport} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public InstanceReport getInstances(final Specification serviceType) throws IOException {
            ensureNonNull("serviceType", serviceType);
            return get(serviceType + "/instances", MediaType.APPLICATION_XML_TYPE).getEntity(InstanceReport.class);
        }

        /**
         * Queries a service metadata from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return a {@link Service} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public Service getMetadata(final Specification serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return get(serviceType + "/" + identifier + "/metadata", MediaType.APPLICATION_XML_TYPE).getEntity(Service.class);
        }

        /**
         * Updates a service metadata on the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param metadata    the service metadata
         * @return an {@link AcknowlegementType} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType setMetadata(final Specification serviceType, final Service metadata) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("metadata",    metadata);
            return post(serviceType + "/metadata", MediaType.APPLICATION_XML_TYPE, metadata).getEntity(AcknowlegementType.class);
        }

        /**
         * Queries a service layer list from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return a {@link LayerList} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public LayerList getLayers(final Specification serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return (LayerList) get(serviceType + "/" + identifier + "/layers", MediaType.APPLICATION_XML_TYPE).getEntity(GenericDatabaseMarshallerPool.getInstance());
        }

        /**
         * Starts a service on the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return an {@link AcknowlegementType} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType start(final Specification serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return get(serviceType + "/" + identifier + "/operation/start", MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        }

        /**
         * Stops a service on the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return an {@link AcknowlegementType} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType stop(final Specification serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return get(serviceType + "/" + identifier + "/operation/stop", MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        }

        /**
         * Restarts a service on the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @param restart     the restart options
         * @return an {@link AcknowlegementType} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType restart(final Specification serviceType, final String identifier, final Restart restart) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return post(serviceType + "/" + identifier + "/operation/restart", MediaType.APPLICATION_XML_TYPE, restart).getEntity(AcknowlegementType.class);
        }
    }

    /**
     * API methods related to providers actions/administration.
     */
    public final class Providers {

        /**
         * Queries the entire list of styles from the Constellation server.
         *
         * @return the list of available styles
         */
        public StyleListBean getStyleList() throws IOException {
            return get("style", MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBean.class);
        }
    }

    /**
     * Submits a HTTP GET request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     */
    private Response get(final String path, final MediaType type) throws IOException {
        return new Response(newRequest(path, type).get(ClientResponse.class));
    }

    /**
     * Submits a HTTP POST request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param body the request entity
     * @return the response instance
     */
    private Response post(final String path, final MediaType type, final Object body) throws IOException {
        return new Response(newRequest(path, type).post(ClientResponse.class, body));
    }

    /**
     * Submits a HTTP PUT request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param body the request entity
     * @return the response instance
     */
    private Response put(final String path, final MediaType type, final Object body) throws IOException {
        return new Response(newRequest(path, type).put(ClientResponse.class, body));
    }

    /**
     * Submits a HTTP DELETE request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param body the request entity
     * @return the response instance
     */
    private Response delete(final String path, final MediaType type, final Object body) throws IOException {
        return new Response(newRequest(path, type).delete(ClientResponse.class, body));
    }

    /**
     * Creates a new request from the specified path and {@link MediaType}.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     */
    private WebResource.Builder newRequest(final String path, final MediaType type) {
        return this.client.resource(url + "api/" + version + "/").path(path).type(type);
    }

    /**
     * {@link ClientResponse} wrapper class for specific response handling.
     */
    private final static class Response {

        /**
         * Wrapped {@link ClientResponse} instance.
         */
        private final ClientResponse response;

        /**
         * Creates a {@link ClientResponse} wrapper instance.
         * 
         * @param response the response to wrap
         */
        public Response(final ClientResponse response) {
            this.response = response;
        }

        /**
         * @see ClientResponse#getEntity(Class)
         *
         * @param <T> the type of the response
         * @param c   the type of the entity
         * @return an instance of the type {@code c}
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public <T> T getEntity(final Class<T> c) throws IOException {
            ensureNonNull("c", c);
            ensureSuccessStatus();
            return response.getEntity(c);
        }

        /**
         * Handles and parses the XML response using a {@link ParameterValueReader} in
         * accordance with the specified {@link ParameterDescriptorGroup}.
         *
         * @param descriptor the parameter value descriptor
         * @return a {@link GeneralParameterValue} instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public GeneralParameterValue getEntity(final ParameterDescriptorGroup descriptor) throws IOException {
            ensureNonNull("descriptor", descriptor);
            ensureSuccessStatus();
            try {
                final ParameterValueReader reader = new ParameterValueReader(descriptor);
                reader.setInput(response.getEntityInputStream());
                return reader.read();
            } catch (XMLStreamException ex) {
                throw new IOException("GeneralParameterValue entity parsing has failed", ex);
            } finally {
                response.close();
            }
        }

        /**
         * Handles and parses the XML response using a specific {@link MarshallerPool}.
         *
         * @param pool the marshaller pool
         * @return a response binding object instance
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public Object getEntity(final MarshallerPool pool) throws IOException {
            ensureNonNull("pool", pool);
            ensureSuccessStatus();
            try {
                final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(response.getEntityInputStream());
                pool.recycle(unmarshaller);
                return obj;
            } catch (JAXBException ex) {
                throw new IOException("XML entity unmarshalling has failed", ex);
            } finally {
                response.close();
            }
        }

        /**
         * Checks if the response has a "ok" status code.
         *
         * @throws IOException if the response does not have a "ok" status code
         */
        private void ensureSuccessStatus() throws IOException {
            if (response.getStatus() >= 300) {
                throw new IOException(response.toString());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return response.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.client.destroy();
    }
}
