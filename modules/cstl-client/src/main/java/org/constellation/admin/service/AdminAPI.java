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
