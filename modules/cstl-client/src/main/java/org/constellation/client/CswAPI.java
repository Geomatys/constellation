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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.BriefNodeList;
import org.constellation.configuration.StringList;
import org.constellation.dto.ParameterValues;
import org.w3c.dom.Node;

/**
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class CswAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    CswAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * path : /1/CSW/{id}/clearCache<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.clearCache<br>
     */
    public void clearCache(){
        throw new UnsupportedOperationException("Not supported yet");
    }


    /**
     * path : /1/CSW/{id}/records/count<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getMetadataCount<br>
     */
    public void getMetadataCount(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/records/{count: \w+}-{startIndex: \w+}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getMetadataList<br>
     */
    public BriefNodeList getMetadataList(final String identifier, final int count, final int startIndex) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records/" + count + "-" + startIndex;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(BriefNodeList.class);
    }

    /**
     * path : /1/CSW/{id}/record/{metaID}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getMetadata<br>
     */
    public Node getMetadata(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/" + metaID;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(Node.class);
    }

    /**
     * path : /1/CSW/{id}/metadata/save/{metaID}/{type}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.CSWServicesRest.saveMetadata<br>
     */
    public void saveMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/index/stop<br>
     * method : POST<br>
     * java : org.constellation.rest.api.CSWServicesRest.stopIndexation<br>
     */
    public void stopIndexation(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/index/{metaID}<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.CSWServicesRest.AddToIndex<br>
     */
    public void AddToIndex(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/index/{metaID}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.CSWServicesRest.removeFromIndex<br>
     */
    public void removeFromIndex(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/index/refresh<br>
     * method : POST<br>
     * java : org.constellation.rest.api.CSWServicesRest.refreshIndex<br>
     */
    public boolean refreshIndex(final String identifier, final boolean asynchrone, final boolean forced) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/index/refresh";
        final ParameterValues values = new ParameterValues();
        values.getValues().put("ASYNCHRONE", Boolean.toString(asynchrone));
        values.getValues().put("FORCED",     Boolean.toString(forced));
        final AcknowlegementType ack = client.post(path, MediaType.APPLICATION_XML_TYPE, values).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }

    /**
     * path : /1/CSW/{id}/record/{metaID}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.CSWServicesRest.removeMetadata<br>
     */
    public boolean removeMetadata(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/" + metaID;
        final AcknowlegementType ack = client.delete(path, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }

    /**
     * path : /1/CSW/{id}/records<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.CSWServicesRest.removeAllMetadata<br>
     */
    public void removeAllMetadata(final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records";
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }

    /**
     * path : /1/CSW/{id}/record/exist/{metaID}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.metadataExist<br>
     */
    public boolean metadataExist(final String identifier, final String metaID) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("metaID",  metaID);

        final String path = "CSW/" + identifier + "/record/exist/" + metaID;
        final AcknowlegementType ack = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(AcknowlegementType.class);
        return "Exist".equals(ack.getStatus());
    }

    /**
     * path : /1/CSW/{id}/record/download/{metaID}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.downloadMetadata<br>
     */
    public void downloadMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/records/{fileName}<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.CSWServicesRest.importRecord<br>
     */
    public boolean importRecord(final String identifier, final File metaFile) throws HttpResponseException, IOException {
        ensureNonNull("identifier",  identifier);

        final String path = "CSW/" + identifier + "/records/" + metaFile.getName();
        final AcknowlegementType ack = client.post(path, MediaType.APPLICATION_XML_TYPE, metaFile).getEntity(AcknowlegementType.class);
        return ack.getStatus().equals("Success");
    }

    /**
     * path : /1/CSW/{id}/records/data/{dataID}<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.CSWServicesRest.importInternalData<br>
     */
    public void importInternalData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/importInternaldata<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.canImportInternalData<br>
     */
    public void canImportInternalData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/types<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getCSWDatasourceType<br>
     */
    public Collection<String> getCSWDatasourceType() throws HttpResponseException, IOException {
        final String path = "CSW/types";
        final StringList sl = client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StringList.class);
        return sl.getList();
    }

    /**
     * path : /1/CSW/{id}/federatedCatalog<br>
     * method : POST<br>
     * java : org.constellation.rest.api.CSWServicesRest.setFederatedCatalog<br>
     */
    public void setFederatedCatalog(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{id}/metadataJson/{metaID}/{type}/{prune}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getCSWMetadataJson<br>
     */
    public void getCSWMetadataJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{serviceID}/mapper<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getMapperContent<br>
     */
    public void getMapperContent(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/CSW/{serviceID}/tree<br>
     * method : GET<br>
     * java : org.constellation.rest.api.CSWServicesRest.getStreeRepresentation<br>
     */
    public void getStreeRepresentation(){
        throw new UnsupportedOperationException("Not supported yet");
    }


}
