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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
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
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;

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
     * API methods related to tasks administration.
     */
    public final TasksAPI tasks;

    /**
     * API methods related to csw administration.
     */
    public final CswAPI csw;
    
    /**
     * API methods related to constellation administration.
     */
    public final AdminAPI admin;

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
        final Configuration config = new ClientConfig(NodeReader.class);
        this.client = ClientBuilder.newClient(config);
        connectTimeout(5000);
        readTimeout(20000);

        this.url        = url.endsWith("/") ? url : url + "/";
        this.version    = version;
        this.services   = new ServicesAPI(this);
        this.providers  = new ProvidersAPI(this);
        this.csw        = new CswAPI(this);
        this.admin      = new AdminAPI(this);
        this.tasks      = new TasksAPI(this);
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
    public ConstellationClient basicAuth(final String login, final String password) {
        ensureNonNull("login",    login);
        ensureNonNull("password", password);
        this.client.register(new HttpBasicAuthFilter(login, password));
        return this;
    }
    
    public ConstellationClient auth(final String login, final String password) {

        String str = url + "j_spring_security_check?";
        InputStream stream = null;
        HttpURLConnection cnx = null;
        try {
            final URL url = new URL(str);
            cnx = (HttpURLConnection) url.openConnection();
            cnx.setDoOutput(true);
            cnx.setInstanceFollowRedirects(false);

            final OutputStream os = cnx.getOutputStream();
            final String s = "j_username=" + login + "&j_password=" + password;
            os.write(s.getBytes());

            stream = cnx.getInputStream();

            String cookie = cnx.getHeaderField("Set-Cookie");
            Pattern pattern = Pattern.compile("JSESSIONID=([^;]+);.*");
            Matcher matcher = pattern.matcher(cookie);
            if(matcher.matches()){
               final String cookieValue = "JSESSIONID=" + matcher.group(1);
               System.out.println("cookie=" + cookieValue);
               this.client.register(new ClientRequestFilter() {
                    private List<Object> cookies = Arrays.asList((Object)cookieValue);

                    @Override
                    public void filter(ClientRequestContext request) throws IOException {
                        if (cookies != null) {
                            request.getHeaders().put("Cookie", cookies);
                        }
                    }
                });
               return this;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            if (cnx != null) {
                cnx.disconnect();
            }
        }
        return null;
    }

    /**
     * Configures the Jersey {@link Client} read timeout for HTTP communication.
     *
     * @param timeout the timeout value (in ms)
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient readTimeout(final int timeout) {
        ensureStrictlyPositive("timeout", timeout);
        this.client.property(ClientProperties.READ_TIMEOUT, timeout);
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
        this.client.property(ClientProperties.CONNECT_TIMEOUT, timeout);
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

    public WebTarget target() {
        return client.target(url);
    }
}
