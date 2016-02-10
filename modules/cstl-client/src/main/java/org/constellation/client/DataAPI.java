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
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;

/**
 *
 */
public class DataAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    DataAPI(final ConstellationClient client) {
        this.client = client;
    }
    
    /**
     * path : /1/domain/{domainId}/data/metadataCodeLists<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getMetadataCodeLists<br>
     */
    public MetadataLists getMetadataCodeLists(final String locale) throws IOException {

        GenericType<JAXBElement<MetadataLists>> planetType = new GenericType<JAXBElement<MetadataLists>>() {
        };

        WebTarget target = client.getWebTarget();
        return (MetadataLists) target.path("api/1/data/metadataCodeLists/fr").request()
                .accept(MediaType.APPLICATION_XML_TYPE).get(planetType).getValue();

        //return client.get("data/metadataCodeLists/" + locale, MediaType.APPLICATION_XML_TYPE).getEntity(MetadataLists.class);
    }

    /**
     * path : /1/domain/{domainId}/data/datapath/{filtered}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getDataFolder<br>
     */
    public List<FileBean> getDataFolder(String path) throws IOException {
        ensureNonNull("path", path);
        final FileListBean list = client.post("data/datapath", MediaType.APPLICATION_XML_TYPE, path).getEntity(FileListBean.class);
        return list.getList();
    }

    /**
     * path : /1/domain/{domainId}/data/metadatapath/{filtered}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getMetaDataFolder<br>
     */
    public void getMetaDataFolder(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/upload/data<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.uploadData<br>
     */
    public DataInformation uploadData(final String filePath, final String metadataFilePath, final String dataType) throws IOException {
        ParameterValues pv = new ParameterValues();
        HashMap<String, String> parameters = new HashMap<>(0);
        parameters.put("filePath", filePath);
        parameters.put("metadataFilePath", metadataFilePath);
        parameters.put("dataType", dataType);
        pv.setValues(parameters);


        GenericType<JAXBElement<DataInformation>> planetType = new GenericType<JAXBElement<DataInformation>>() {
        };



        return client.getWebTarget().path("api/1/data/load").request().accept(MediaType.APPLICATION_XML_TYPE).post(Entity.entity(pv, MediaType.APPLICATION_XML_TYPE),planetType).getValue();

        //return client.post("data/load", MediaType.APPLICATION_XML_TYPE, pv).getEntity(DataInformation.class);
    }

    /**
     * path : /1/domain/{domainId}/data/upload/metadata<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.uploadMetadata<br>
     */
    public void uploadMetadata(final DataMetadata metadataToSave) throws IOException {
        client.post("data/upload/metadata", MediaType.APPLICATION_XML_TYPE, metadataToSave);
    }

    /**
     * path : /1/domain/{domainId}/data/import/full<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.proceedToImport<br>
     */
    public void proceedToImport(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/listStoreConfigurations<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getAllDataStoreConfigurations<br>
     */
    public void getAllDataStoreConfigurations(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/putStoreConfigurations<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.putDataStoreConfiguration<br>
     */
    public void putDataStoreConfiguration(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/pyramid/folder/{id}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.DataRest.deletePyramidFolder<br>
     */
    public void deletePyramidFolder(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/upload<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.saveUploadedMetadata<br>
     */
    public void saveUploadedMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/dataset<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getDatasetMetadata<br>
     */
    public void getDatasetMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/data<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getDataMetadata<br>
     */
    public void getDataMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadataJson/iso/{providerId}/{dataId}/{type}/{prune}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getIsoMetadataJson<br>
     */
    public void getIsoMetadataJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadataJson/dataset/iso/{datasetIdentifier}/{type}/{prune}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getIsoMetadataJsonDS<br>
     */
    public void getIsoMetadataJsonDS(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/merge/{provider}/{identifier}/{type}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.mergeMetadata<br>
     */
    public void mergeMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/dataset/merge/{identifier}/{type}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.mergeMetadataDS<br>
     */
    public void mergeMetadataDS(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/find<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.findMetadata<br>
     */
    public void findMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.initMetadataFromReader<br>
     */
    public void initMetadataFromReader(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/pyramid/createconform/{providerId}/{dataName}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.createTiledProviderConform<br>
     */
    public void createTiledProviderConform(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/pyramid/mapcontext/{contextId}/{crs}/{layerName}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.pyramidMapContext<br>
     */
    public void pyramidMapContext(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/pyramid/create/{crs}/{layerName}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.pyramidData<br>
     */
    public void pyramidData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/pyramid/bestscales/{providerId}/{dataId}/{crs}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.findBestScales<br>
     */
    public void findBestScales(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/coverage/list/<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getCoverageList<br>
     */
    public ParameterValues getCoverageList(final String providerId) throws IOException {
        final SimpleValue value = new SimpleValue(providerId);
        final ParameterValues coverageList = client.post("data/coverage/list/", MediaType.APPLICATION_XML_TYPE, value).getEntity(ParameterValues.class);
        return coverageList;
    }

    /**
     * path : /1/domain/{domainId}/data/summary<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.DataRest.getDataSummary<br>
     */
    public DataBrief getDataSummary(final String name, final String namespace, final String providerId) throws IOException {
        final HashMap<String, String> values = new HashMap<>(0);
        values.put("namespace", namespace);
        values.put("name", name);
        values.put("providerId", providerId);
        return client.put("data/summary/", MediaType.APPLICATION_XML_TYPE, new ParameterValues(values)).getEntity(DataBrief.class);
    }

    /**
     * path : /1/domain/{domainId}/data/list<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getDataList<br>
     */
    public void getDataList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/{type}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getDataList<br>
     */
    public void getDataList2(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/provider<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getDataListsForProviders<br>
     */
    public void getDataListsForProviders(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/provider/{providerId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getDataListsForProviders<br>
     */
    public void getDataListsForProviders2(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/count/all<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getCountAll<br>
     */
    public void getCountAll(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/top<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getTopDataList<br>
     */
    public void getTopDataList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/top/{type}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getTopDataList<br>
     */
    public void getTopDataList2(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/dataset<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getDatasetList<br>
     */
    public void getDatasetList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/published/{published}/data<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getPublishedDataList<br>
     */
    public void getPublishedDataList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/published/{published}/dataset<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getPublishedDatasetList<br>
     */
    public void getPublishedDatasetList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/observation/{sensorable}/data<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getSensorableDataList<br>
     */
    public void getSensorableDataList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/list/observation/{sensorable}/dataset<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getSensorableDatasetList<br>
     */
    public void getSensorableDatasetList(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/include/{dataId}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.includeData<br>
     */
    public void includeData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/{dataId}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.DataRest.removeData<br>
     */
    public void removeData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/layer/summary/{providerid}/{layerAlias}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getLayerSummary<br>
     */
    public DataBrief getLayerSummary(final String layerAlias, final String providerId) throws IOException {
        return client.get("data/layer/summary/"+providerId+"/"+layerAlias, MediaType.APPLICATION_XML_TYPE).getEntity(DataBrief.class);
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/iso/{providerId}/{dataId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getIsoMetadata<br>
     */
    public void getIsoMetadata(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/iso/download/{providerId}/{dataId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.downloadMetadataForData<br>
     */
    public void downloadMetadataForData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/metadata/associated<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.getAssociatedData<br>
     */
    public void getAssociatedData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/testextension<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.testExtensio<br>
     */
    public void testExtensio(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/export/{providerId}/{dataId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.exportData<br>
     */
    public void exportData(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/link/sensor/{providerId}/{dataId}/{sensorId}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.linkDataToSensor<br>
     */
    public void linkDataToSensor(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/unlink/sensor/{providerId}/{dataId}/{sensorId}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.DataRest.unlinkDataToSensor<br>
     */
    public void unlinkDataToSensor(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/{id}/vector/columns<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getVectorDataColumns<br>
     */
    public void getVectorDataColumns(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/{dataId}/associations<br>
     * method : GET<br>
     * java : org.constellation.rest.api.DataRest.getAssociations<br>
     */
    public void getAssociations(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/{dataId}/associations/styles/{styleId}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.DataRest.deleteStyleAssociation<br>
     */
    public void deleteStyleAssociation(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/domain/{domainId}/data/{dataId}/associations/sensors/{sensorId}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.DataRest.deleteSensorAssociation<br>
     */
    public void deleteSensorAssociation(){
        throw new UnsupportedOperationException("Not supported yet");
    }
    

}
