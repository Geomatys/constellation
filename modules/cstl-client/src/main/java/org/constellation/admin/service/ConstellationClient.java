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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;
import org.constellation.ws.rs.ServiceType;

import javax.ws.rs.core.MediaType;
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
         * Queries a service metadata from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param metadata    the service metadata
         * @return the status response
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType newInstance(final ServiceType serviceType, final Service metadata) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("metadata",    metadata);
            return post(serviceType + "/create", MediaType.APPLICATION_XML_TYPE, metadata, AcknowlegementType.class);
        }

        /**
         * Queries a service metadata from the Constellation server.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param identifier  the service identifier
         * @return the service metadata
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public Service getMetadata(final ServiceType serviceType, final String identifier) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("identifier",  identifier);
            return get(serviceType + "/metadata", MediaType.APPLICATION_XML_TYPE, Service.class);
        }

        /**
         * Updates an existing service metadata.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param metadata    the service metadata
         * @return the status response
         * @throws IOException on HTTP communication error or response entity parsing error
         */
        public AcknowlegementType setMetadata(final ServiceType serviceType, final Service metadata) throws IOException {
            ensureNonNull("serviceType", serviceType);
            ensureNonNull("metadata",    metadata);
            return post(serviceType + "/configure", MediaType.APPLICATION_XML_TYPE, metadata, AcknowlegementType.class);
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
            return get("style", MediaType.APPLICATION_XML_TYPE, StyleListBean.class);
		}
    }

    /**
     * Submits a HTTP GET request and returns the result.
     *
     * @param path   the request path
     * @param type   the submitted/expected media type
     * @param _class the expected response {@link Class}
     * @return the response binding object
     */
    private <T> T get(final String path, final MediaType type, final Class<T> _class) throws IOException {
        return handleResponse(newRequest(path, type).get(ClientResponse.class), _class);
    }

    /**
     * Submits a HTTP POST request and returns the result.
     *
     * @param path   the request path
     * @param type   the submitted/expected media type
     * @param _class the expected response {@link Class}
     * @param body   the request entity
     * @return the response binding object
     */
    private <T> T post(final String path, final MediaType type, final Object body, final Class<T> _class) throws IOException {
        return handleResponse(newRequest(path, type).post(ClientResponse.class, body), _class);
    }

    /**
     * Creates a new request from the specified path and {@link MediaType}.
     *
     * @param path the request path
     * @param type the submitted/expected media type
     * @return the request base
     */
    private WebResource.Builder newRequest(final String path, final MediaType type) {
        return this.client.resource(url + "api/" + version + "/").path(path).type(type);
    }

    /**
     * Handles an {@link ClientResponse} to check its status and try to parse its entity.
     *
     * @param response the response to handle
     * @param _class   the expected response {@link Class}
     * @param <T>      the expected response {@link Class}
     * @return the response binding object
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    private static <T> T handleResponse(final ClientResponse response, final Class<T> _class) throws IOException {
        if (response.getStatus() >= 300) {
            throw new IOException(response.toString());
        }
        try {
            return response.getEntity(_class);
        } catch (ClientHandlerException ex) {
            throw new IOException("Response entity parsing has failed", ex);
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
