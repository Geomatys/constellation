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
import javax.ws.rs.core.MediaType;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.SimpleValue;

/**
 *
 */
public class AdminAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    AdminAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * path : /1/admin//property/{key}
     * method : GET
     * java : org.constellation.rest.api.AdminRest.getKey
     */
    public String getKey(final String key) throws HttpResponseException, IOException {
        ensureNonNull("key",  key);

        final String path = "admin/property/" + key;
        final SimpleValue value = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(SimpleValue.class);
        return value.getValue();
    }

    /**
     * path : /1/admin//domain/{domainId}/service/layers/{lang}
     * method : GET
     * java : org.constellation.rest.api.AdminRest.listServiceLayers
     */
    public void listServiceLayers(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/admin//serviceType
     * method : GET
     * java : org.constellation.rest.api.AdminRest.serviceType
     */
    public void serviceType(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/admin//configurationLocation
     * method : GET
     * java : org.constellation.rest.api.AdminRest.configurationPath
     */
    public String configurationPath() throws HttpResponseException, IOException {

        final String path = "admin//configurationLocation";
        final AcknowlegementType value = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return value.getMessage();
    }

    /**
     * path : /1/admin//domain/{domainId}/instances/{lang}
     * method : GET
     * java : org.constellation.rest.api.AdminRest.listInstances
     */
    public void listInstances(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/admin//domain/{domainId}/instances
     * method : GET
     * java : org.constellation.rest.api.AdminRest.listInstances
     */
    public void listInstances2(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/admin//domain/{domainId}/instances/{lang}/{type}
     * method : GET
     * java : org.constellation.rest.api.AdminRest.listInstancesByType
     */
    public void listInstancesByType(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/admin//property/{key}
     * method : POST
     * java : org.constellation.rest.api.AdminRest.setKey
     */
    public void setKey(final String key, final String value) throws HttpResponseException, IOException {
        ensureNonNull("key",  key);

        final String path = "admin/property/" + key;
        client.post(path, MediaType.APPLICATION_XML_TYPE, new SimpleValue(value));
    }

}
