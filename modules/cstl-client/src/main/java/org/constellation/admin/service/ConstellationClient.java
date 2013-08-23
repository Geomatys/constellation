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
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.Service;
import org.constellation.ws.rs.ServiceType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

/**
 * @author Bernard Fabien (Geomatys).
 * @since 0.9
 */
public final class ConstellationClient {

    private static final MessageFormat TEMPLATE_SERVICES_SET_METADATA = new MessageFormat("{0}/configure");

    private final Client client;
    private final String url;
    private final String version;
    public final Services services;
    public final Providers providers;


    /**
     * Creates a new client instance ready to communicate with the Constellation server.
     *
     * @param url     the constellation server root url
     * @param version the constellation configuration API version
     */
    public ConstellationClient(final String url, final String version) {
        this.url        = url.endsWith("/") ? url : url + "/";
        this.version    = version;
        this.services   = new Services();
        this.providers  = new Providers();

        // Initialize Jerzey client.
        this.client = Client.create();
        this.client.setReadTimeout(1000);
        this.client.setConnectTimeout(5000);
    }

    /**
     * Authenticates an user before trying to communicate with the Constellation server.
     *
     * @param login    the user login
     * @param password the user password
     * @return the {@link ConstellationClient} instance
     */
    public ConstellationClient auth(final String login, final String password) {
        this.client.addFilter(new HTTPBasicAuthFilter(login, password));
        return this;
    }

    public final class Services {

    }

    public final class Providers {

        /**
         * Updates an existing service metadata.
         *
         * @param serviceType the service type (WMS, CSW, WPS...)
         * @param metadata    the service metadata
         * @return {@code true} on success, otherwise {@code false}
         */
        public boolean setMetadata(final ServiceType serviceType, final Service metadata) {
            final URI uri = formatURI(TEMPLATE_SERVICES_SET_METADATA, serviceType);

            final AcknowlegementType response;
            try {
                response = post(uri, MediaType.APPLICATION_XML_TYPE, metadata, AcknowlegementType.class);
            } catch (IOException ex) {
                return false;
            }
            return "success".equalsIgnoreCase(response.getMessage());
        }
    }

    private URI formatURI(final MessageFormat template, final Object args) {
        return URI.create(this.url + "api/" + version + "/" + template.format(args));
    }

    public <T> T get(final URI uri, final MediaType mediaType, final Class<T> _class) throws IOException {
        final ClientResponse response = this.client.resource(uri).type(mediaType).get(ClientResponse.class);
        return handleResponse(response, _class);
    }

    public <T> T post(final URI uri, final MediaType mediaType, final Object body, final Class<T> _class) throws IOException {
        final ClientResponse response = this.client.resource(uri).type(mediaType).post(ClientResponse.class, body);
        return handleResponse(response, _class);
    }

    private static <T> T handleResponse(final ClientResponse response, final Class<T> _class) throws IOException {
        if (response.getStatus() >= 300) {
            final Response.Status status = Response.Status.fromStatusCode(response.getStatus());
            throw new IOException("HTTP " + response.getStatus() + " " + status.getReasonPhrase());
        }
        return response.getEntity(_class);
    }
}
