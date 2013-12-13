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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.AcknowlegementType;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensureStrictlyPositive;

/**
 * Constellation RESTful API client.
 *
 * @author Bernard Fabien (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ConstellationClient {

    private static final Logger LOGGER = Logging.getLogger(ConstellationClient.class);

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
    public final ServicesAPI services;

    /**
     * API methods related to providers administration.
     */
    public final ProvidersAPI providers;


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

        // Initialize Jersey client.
        this.client = Client.create();
        connectTimeout(5000);
        readTimeout(20000);

        this.url        = url.endsWith("/") ? url : url + "/";
        this.version    = version;
        this.services   = new ServicesAPI(this);
        this.providers  = new ProvidersAPI(this);
    }

    public String getUrl() {
        return url;
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
     * Submits a HTTP GET request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    Response get(final String path, final MediaType type) throws IOException {
        try {
            return new Response(newRequest(path, type).get(ClientResponse.class));
        } catch (ClientHandlerException | UniformInterfaceException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Submits a HTTP POST request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param body the request entity
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    Response post(final String path, final MediaType type, final Object body) throws IOException {
        try {
            return new Response(newRequest(path, type).post(ClientResponse.class, body));
        } catch (ClientHandlerException | UniformInterfaceException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Submits a HTTP PUT request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param body the request entity
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    Response put(final String path, final MediaType type, final Object body) throws IOException {
        try {
            return new Response(newRequest(path, type).put(ClientResponse.class, body));
        } catch (ClientHandlerException | UniformInterfaceException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }


    /**
     * Submits a HTTP DELETE request and returns the response.
     *
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param map
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    Response delete(final String path, final MediaType type, final MultivaluedMap<String, String> map) throws IOException {
        try {
            final ClientResponse request = this.client
                    .resource(url + "api/" + version + "/")
                    .queryParams(map)
                    .path(path)
                    .type(type)
                    .delete(ClientResponse.class);
            return new Response(request);
        } catch (ClientHandlerException | UniformInterfaceException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Creates a new request from the specified path and {@link MediaType}.
     *
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
    final static class Response {

        /**
         * Wrapped {@link ClientResponse} instance.
         */
        private final ClientResponse response;

        /**
         * Creates a {@link ClientResponse} wrapper instance.
         *
         * @param response the response to wrap
         */
        private Response(final ClientResponse response) {
            ensureNonNull("response", response);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(response.toString());
            }
            this.response = response;
        }

        /**
         * @see ClientResponse#getEntity(Class)
         *
         * @param <T> the type of the response
         * @param c   the type of the entity
         * @return an instance of the type {@code c}
         * @throws HttpResponseException if the response does not have a {@code 2xx} status code
         * @throws IOException if the response entity parsing has failed
         */
        public <T> T getEntity(final Class<T> c) throws HttpResponseException, IOException {
            ensureNonNull("c", c);
            ensure2xxStatus();
            ensureNonEmptyContent();
            try {
                return response.getEntity(c);
            } catch (ClientHandlerException | UniformInterfaceException ex) {
                throw new IOException("Response entity processing has failed.", ex);
            } finally {
                response.close();
            }
        }

        /**
         * Handles and parses the XML response using a {@link ParameterValueReader} in
         * accordance with the specified {@link ParameterDescriptorGroup}.
         *
         * @param descriptor the parameter value descriptor
         * @return a {@link GeneralParameterValue} instance
         * @throws HttpResponseException if the response does not have a {@code 2xx} status code
         * @throws IOException if the response entity parsing has failed
         */
        public GeneralParameterValue getEntity(final ParameterDescriptorGroup descriptor) throws HttpResponseException, IOException {
            ensureNonNull("descriptor", descriptor);
            ensure2xxStatus();
            ensureNonEmptyContent();
            try {
                final ParameterValueReader reader = new ParameterValueReader(descriptor);
                reader.setInput(response.getEntityInputStream());
                return reader.read();
            } catch (XMLStreamException ex) {
                throw new IOException("GeneralParameterValue entity parsing has failed.", ex);
            } finally {
                response.close();
            }
        }

        /**
         * Handles and parses the XML response using a specific {@link MarshallerPool}.
         *
         * @param pool the marshaller pool
         * @return a response binding object instance
         * @throws HttpResponseException if the response does not have a {@code 2xx} status code
         * @throws IOException if the response entity parsing has failed
         */
        public Object getEntity(final MarshallerPool pool) throws HttpResponseException, IOException  {
            ensureNonNull("pool", pool);
            ensure2xxStatus();
            ensureNonEmptyContent();
            try {
                final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
                final Object obj = unmarshaller.unmarshal(response.getEntityInputStream());
                pool.recycle(unmarshaller);
                return obj;
            } catch (JAXBException ex) {
                throw new IOException("XML entity unmarshalling has failed.", ex);
            } finally {
                response.close();
            }
        }

        /**
         * Ensures that the response has a "success" status code {@code 2xx}.
         *
         * @throws HttpResponseException if the response does not have a {@code 2xx} status code 
         * @throws IOException if the response entity parsing has failed
         */
        public void ensure2xxStatus() throws HttpResponseException, IOException  {
            if (response.getStatus() / 100 != 2) {
                final String message;
                if (MediaType.TEXT_PLAIN_TYPE.equals(response.getType())) {
                    message = response.getEntity(String.class);
                } else if (MediaType.TEXT_XML_TYPE.equals(response.getType())
                        || MediaType.APPLICATION_XML_TYPE.equals(response.getType())
                        || MediaType.APPLICATION_JSON_TYPE.equals(response.getType())) {
                    message = response.getEntity(AcknowlegementType.class).getMessage();
                } else {
                    message = response.toString();
                }
                throw new HttpResponseException(response.getStatus(), message);
            }
        }

        /**
         * Ensures that the response has a non empty entity.
         *
         * @throws IOException if the response does not have a {@code 200} status code
         */
        private void ensureNonEmptyContent() throws IOException {
            if (response.getStatus() == 204) {
                throw new IOException("Empty response entity.");
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
