/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

import java.io.IOException;
import javax.ws.rs.core.MediaType;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.dto.SimpleValue;

/**
 *
 * @author Guilhem Legal
 */
public class AdminAPI {
    
     /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     * Creates a {@link ServicesAPI} instance.
     *
     * @param client the client to use
     */
    AdminAPI(final ConstellationClient client) {
        this.client = client;
    }
    
    public String getProperty(final String key) throws HttpResponseException, IOException {
        ensureNonNull("key",  key);

        final String path = "admin/property/" + key;
        final SimpleValue value = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(SimpleValue.class);
        return value.getValue();
    }
    
    public void setProperty(final String key, final String value) throws HttpResponseException, IOException {
        ensureNonNull("key",  key);

        final String path = "admin/property/" + key;
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(value));
    }

}
