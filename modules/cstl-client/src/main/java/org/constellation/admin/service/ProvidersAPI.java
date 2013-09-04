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

import org.constellation.dto.StyleListBean;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Constellation RESTful API for providers management/configuration.
 *
 * @author Bernard Fabien (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class ProvidersAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     * Creates a {@link ProvidersAPI} instance.
     *
     * @param client the client to use
     */
    ProvidersAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * Queries the entire list of styles from the Constellation server.
     *
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws java.io.IOException on HTTP communication error or response entity parsing error
     */
    public StyleListBean getStyles() throws HttpResponseException, IOException {
        return client.get("style", MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBean.class);
    }
}
