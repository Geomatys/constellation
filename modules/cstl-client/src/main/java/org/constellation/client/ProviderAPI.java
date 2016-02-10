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
import org.apache.sis.metadata.iso.DefaultMetadata;

/**
 *
 */
public class ProviderAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    ProviderAPI(final ConstellationClient client) {
        this.client = client;
    }
    
    /**
     * path : /1/domain/{domainId}/provider//{id}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.create<br>
     */
    public void create(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/{id}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.ProviderRest.delete<br>
     */
    public void delete(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider//{id}<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.ProviderRest.update<br>
     */
    public void update(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider//{id}/test<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.test<br>
     */
    public void test(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider//{id}/epsgCode<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.getAllEpsgCode<br>
     */
    public void getAllEpsgCode(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider//{id}/createprj<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.createPrj<br>
     */
    public void createPrj(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider//{id}/crs<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.verifyCRS<br>
     */
    public void verifyCRS(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/dataDescription<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.dataDescription<br>
     */
    public void dataDescription(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/dataGeographicExtent<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.dataGeographicExtent<br>
     */
    public void dataGeographicExtent(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/mergedDataGeographicExtent<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.mergedDataGeographicExtent<br>
     */
    public void mergedDataGeographicExtent(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/{id}/{layerName}/{property}/propertyValues<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.propertyValues<br>
     */
    public void propertyValues(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/{id}/{layerName}/isGeophysic<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.isGeophysic<br>
     */
    public void isGeophysic(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/{id}/{layerName}/listPyramidChoice<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.listPyramids<br>
     */
    public void listPyramids(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/{id}/{layerName}/{bandIndex}/bandValues<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.bandValues<br>
     */
    public void bandValues(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/provider/metadata/{providerId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.ProviderRest.getMetadata<br>
     */
    public DefaultMetadata getMetadata(final String providerId) throws IOException {
        return client.get("provider/metadata/"+providerId, MediaType.APPLICATION_XML_TYPE).getEntity(DefaultMetadata.class);
    }

    /**
     * path : /1/domain/{domainId}/provider/metadata/{providerId}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.ProviderRest.setMetadata<br>
     */
    public void setMetadata(final String providerId, final DefaultMetadata metadata) throws IOException {
        client.post("provider/metadata/"+providerId, MediaType.APPLICATION_XML_TYPE, metadata);
    }



}
