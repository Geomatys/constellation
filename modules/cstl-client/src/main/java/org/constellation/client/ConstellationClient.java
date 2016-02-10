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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensureStrictlyPositive;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AcknowlegementType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

/**
 *
 * @author Bernard Fabien (Geomatys)
 * @author Benjamin Garcia (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class ConstellationClient {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.admin.service");

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

    public final AdminAPI adminApi;
    public final CswAPI cswApi;
    public final DataAPI dataApi;
    public final DataSetAPI datasetApi;
    public final MapAPI mapApi;
    public final MapContextAPI mapcontextApi;
    public final MetadataAPI metadataApi;
    public final PortrayalAPI portrayalApi;
    public final ProviderAPI providerApi;
    public final SensorAPI sensorApi;
    public final ServicesAPI servicesApi;
    public final SosAPI sosApi;
    public final StyleAPI styleApi;
    public final TaskAPI taskApi;
    public final UserAPI userApi;
    public final WpsAPI wpsApi;

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
        final Configuration config = new ClientConfig(NodeReader.class, ParameterValueGroupWriter.class);
        this.client = ClientBuilder.newClient(config);
        setConnectTimeout(5000);
        setReadTimeout(20000);

        this.url        = url.endsWith("/") ? url : url + "/";
        this.version    = version;

        adminApi        = new AdminAPI(this);
        cswApi          = new CswAPI(this);
        dataApi         = new DataAPI(this);
        datasetApi      = new DataSetAPI(this);
        mapApi          = new MapAPI(this);
        mapcontextApi   = new MapContextAPI(this);
        metadataApi     = new MetadataAPI(this);
        portrayalApi    = new PortrayalAPI(this);
        providerApi     = new ProviderAPI(this);
        sensorApi       = new SensorAPI(this);
        servicesApi     = new ServicesAPI(this);
        sosApi          = new SosAPI(this);
        styleApi        = new StyleAPI(this);
        taskApi         = new TaskAPI(this);
        userApi         = new UserAPI(this);
        wpsApi          = new WpsAPI(this);

    }

    public String getUrl() {
        return url;
    }

    public WebTarget getWebTarget() {
        return client.target(url);
    }

    /**
     * Authenticates an user before trying to communicate with the Constellation server.
     *
     * @param login    the user login
     * @param password the user password
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient authenticate(final String login, final String password) throws IOException {
        ensureNonNull("login",    login);
        ensureNonNull("password", password);
        final String token = TokenAuthenticator.requestToken(url, login, password);
        this.client.register(new TokenAuthenticator(token));
        return this;
    }

    /**
     * Configures the Jersey {@link Client} read timeout for HTTP communication.
     *
     * @param timeout the timeout value (in ms)
     */
    public void setReadTimeout(final int timeout) {
        ensureStrictlyPositive("timeout", timeout);
        this.client.property(ClientProperties.READ_TIMEOUT, timeout);
    }

    /**
     * Configures the Jersey {@link Client} connection timeout for HTTP communication.
     *
     * @param timeout the timeout value (in ms)
     */
    public void setConnectTimeout(final int timeout) {
        ensureStrictlyPositive("timeout", timeout);
        this.client.property(ClientProperties.CONNECT_TIMEOUT, timeout);
    }

    /**
     * Submits a HTTP GET request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    ResponseContainer get(final String path, final MediaType type) throws IOException {
        try {
            return new ResponseContainer(newRequest(path, type).get(Response.class));
        } catch (ProcessingException | WebApplicationException ex) {
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
    ResponseContainer post(final String path, final MediaType type, final Object body) throws IOException {
        try {
            return new ResponseContainer(newRequest(path, type).post(Entity.entity(body, type), Response.class));
        } catch (ProcessingException | WebApplicationException ex) {
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
    ResponseContainer put(final String path, final MediaType type, final Object body) throws IOException {
        try {
            return new ResponseContainer(newRequest(path, type).put(Entity.entity(body, type), Response.class));
        } catch (ProcessingException | WebApplicationException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Submits a HTTP DELETE request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    ResponseContainer delete(final String path, final MediaType type) throws IOException {
        try {
            return new ResponseContainer(newRequest(path, type).delete(Response.class));
        } catch (ProcessingException | WebApplicationException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Submits a HTTP DELETE request and returns the response.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @param paramName parameter send name
     * @param paramValue parameter send
     * @return the response instance
     * @throws IOException on HTTP communication problem like connection or read timeout
     */
    ResponseContainer delete(final String path, final MediaType type, String paramName, String paramValue) throws IOException {
        try {
            return new ResponseContainer(newRequest(path, type, paramName, paramValue).delete(Response.class));
        } catch (ProcessingException | WebApplicationException ex) {
            throw new IOException("An error occurred during HTTP communication with the Constellation server.", ex);
        }
    }

    /**
     * Creates a new request from the specified path and {@link MediaType}.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the response instance
     */
    private Invocation.Builder newRequest(final String path, final MediaType type) {
        return this.client.target(url + "api/" + version + "/").path(path).request(type);
    }

    private Invocation.Builder newRequest(final String path, final MediaType type, String paramName, String paramValue) {
        return this.client.target(url + "api/" + version + "/").queryParam(paramName, paramValue).path(path).request(type);
    }

    /**
     * {@link Response} wrapper class for specific response handling.
     */
    final static class ResponseContainer {

        /**
         * Wrapped {@link Response} instance.
         */
        private final Response response;

        /**
         * Creates a {@link Response} wrapper instance.
         *
         * @param response the response to wrap
         */
        private ResponseContainer(final Response response) {
            ensureNonNull("response", response);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(response.toString());
            }
            this.response = response;
        }

        /**
         * @see ClientResponse#getEntity()
         *
         * @param <T> the type of the response
         * @param c   the type of the entity
         * @return an instance of the type {@code c}
         * @throws HttpResponseException if the response does not have a {@code 2xx} status code
         * @throws IOException if the response entity parsing has failed
         */
        public <T> T getEntity(final Class<T> c) throws HttpResponseException, IOException {
            ensureNonNull("c", c);
            try {
                return response.readEntity(c);
            } catch (ProcessingException | WebApplicationException ex) {
                throw new IOException("Response entity processing has failed.", ex);
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
                if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
                    message = response.readEntity(String.class);
                } else if (MediaType.TEXT_XML_TYPE.equals(response.getMediaType())
                        || MediaType.APPLICATION_XML_TYPE.equals(response.getMediaType())
                        || MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
                    message = response.readEntity(AcknowlegementType.class).getMessage();
                } else {
                    message = response.toString();
                }
                throw new HttpResponseException(response.getStatus(), message);
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
        this.client.close();
    }

}
