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
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import org.apache.sis.metadata.iso.DefaultMetadata;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.StyleReport;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.dto.StyleListBrief;
import org.geotoolkit.style.DefaultMutableStyle;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.xml.parameter.ParameterDescriptorReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Constellation RESTful API for providers management/configuration.
 *
 * @author Bernard Fabien (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class ProvidersAPI {

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
     * Queries the entire list of styles.
     *
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleListBrief getStyles(final String category) throws HttpResponseException, IOException {
        String path = "SP/all/style/available";
        if(category != null && !category.isEmpty()){
            path += "/"+category;
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBrief.class);
    }

    /**
     * Queries the entire list of styles from the specified provider.
     *
     * @param providerId the style provider identifier
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleListBrief getStyles(final String providerId, final String category) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        String path = "SP/" + providerId + "/style/available";
        if(category != null && !category.isEmpty()){
            path += "/"+category;
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBrief.class);
    }

    /**
     * Deletes a style from a style provider.
     *
     * @param providerId the style provider identifier
     * @param styleName  the style name
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public void deleteStyle(final String providerId, final String styleName) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);

        final String path = "SP/" + providerId + "/style/" + styleName;
        client.delete(path, MediaType.APPLICATION_XML_TYPE).ensure2xxStatus();
    }

    /**
     * Queries a style report that contains several information of a style resource.
     *
     * @param providerId the style provider identifier
     * @param styleName  the style name
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleReport getStyleReport(final String providerId, final String styleName) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);

        final String path = "SP/" + providerId + "/style/" + styleName + "/report";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleReport.class);
    }
    
    public MutableStyle getStyle(final String providerId, final String styleName) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);

        final String path = "SP/" + providerId + "/style/" + styleName;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(MutableStyle.class);
    }

    public void createStyle(final String providerId, final MutableStyle style) throws IOException {
        final String path = "SP/" + providerId + "/style";
        client.put(path, MediaType.APPLICATION_XML_TYPE, style);
    }
    
    /**
     * Links a style resource to an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleName     the style name
     * @param dataProvider  the data provider identifier
     * @param dataName      the data name
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public void linkStyleToData(final String styleProvider, final String styleName, final String dataProvider, final String dataName, final String namespace) throws HttpResponseException, IOException {
        ensureNonNull("styleProvider", styleProvider);
        ensureNonNull("styleName", styleName);
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataName", dataName);

        final ParameterValues values = new ParameterValues();
        values.getValues().put("dataProvider", dataProvider);
        values.getValues().put("dataNamespace", namespace);
        values.getValues().put("dataId", dataName);

        final String path = "SP/" + styleProvider + "/style/" + styleName + "/linkData";
        client.post(path, MediaType.APPLICATION_XML_TYPE, values).ensure2xxStatus();
    }

    /**
     * Unlink a style resource to an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleName     the style name
     * @param dataProvider  the data provider identifier
     * @param dataName      the data name
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public void unlinkStyleFromData(final String styleProvider, final String styleName, final String dataProvider, final String dataName, final String namespace) throws IOException {
        ensureNonNull("styleProvider", styleProvider);
        ensureNonNull("styleName", styleName);
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataName", dataName);

        final ParameterValues values = new ParameterValues();
        values.getValues().put("dataProvider", dataProvider);
        values.getValues().put("dataNamespace", namespace);
        values.getValues().put("dataId", dataName);

        final String path = "SP/" + styleProvider + "/style/" + styleName + "/unlinkData";
        client.post(path, MediaType.APPLICATION_XML_TYPE, values).ensure2xxStatus();
    }

    /**
     * @param path
     * @return
     * @throws IOException
     */
    public List<FileBean> getDataFolder(String path) throws IOException {
        ensureNonNull("path", path);
        final FileListBean list = client.post("data/datapath", MediaType.APPLICATION_XML_TYPE, path).getEntity(FileListBean.class);
        return list.getList();
    }

    public DataInformation loadData(final String filePath, final String metadataFilePath, final String dataType) throws IOException {
        ParameterValues pv = new ParameterValues();
        HashMap<String, String> parameters = new HashMap<>(0);
        parameters.put("filePath", filePath);
        parameters.put("metadataFilePath", metadataFilePath);
        parameters.put("dataType", dataType);
        pv.setValues(parameters);


        GenericType<JAXBElement<DataInformation>> planetType = new GenericType<JAXBElement<DataInformation>>() {
        };
       
        
        
        return client.target().path("api/1/data/load").request().accept(MediaType.APPLICATION_XML_TYPE).post(Entity.entity(pv, MediaType.APPLICATION_XML_TYPE),planetType).getValue();

        //return client.post("data/load", MediaType.APPLICATION_XML_TYPE, pv).getEntity(DataInformation.class);
    }

    /**
     *
     * @param locale
     * @return
     * @throws IOException
     */
    public MetadataLists getMetadataCodeLists(final String locale) throws IOException {
    
        GenericType<JAXBElement<MetadataLists>> planetType = new GenericType<JAXBElement<MetadataLists>>() {
        };
        
        WebTarget target = client.target();
        return (MetadataLists) target.path("api/1/data/metadataCodeLists/fr").request()
                .accept(MediaType.APPLICATION_XML_TYPE).get(planetType).getValue();
        
        //return client.get("data/metadataCodeLists/" + locale, MediaType.APPLICATION_XML_TYPE).getEntity(MetadataLists.class);
    
    
    }

    /**
     *
     * @param metadataToSave
     * @throws IOException
     */
    public void saveISO19115Metadata(final DataMetadata metadataToSave) throws IOException {
        client.post("data/metadata", MediaType.APPLICATION_XML_TYPE, metadataToSave);
    }

    /**
     *
     * @param name
     * @param pPath
     * @throws IOException
     */
    public void pyramidData(final String name, final String pPath) throws IOException {
        final SimpleValue path = new SimpleValue(pPath);
        client.post("data/pyramid/"+name, MediaType.APPLICATION_XML_TYPE, path);
    }

    public ParameterValues getCoverageList(final String providerId) throws IOException {
        SimpleValue value = new SimpleValue(providerId);
        ParameterValues coverageList = client.post("data/coverage/list/", MediaType.APPLICATION_XML_TYPE, value).getEntity(ParameterValues.class);
        return coverageList;
    }

    /**
     *
     * @param values
     */
    public void saveCRSModification(final ParameterValues values) throws IOException {
       client.post("crs/update", MediaType.APPLICATION_XML_TYPE, values);
    }

    public DataBrief getDataSummary(final String name, final String namespace, final String providerId) throws IOException {
        final HashMap<String, String> values = new HashMap<>(0);
        values.put("namespace", namespace);
        values.put("name", name);
        values.put("providerId", providerId);
        return client.put("data/summary/", MediaType.APPLICATION_XML_TYPE, new ParameterValues(values)).getEntity(DataBrief.class);
    }

    public DataBrief getLayerSummary(final String layerAlias, final String providerId) throws IOException {
        return client.get("data/layer/summary/"+providerId+"/"+layerAlias, MediaType.APPLICATION_XML_TYPE).getEntity(DataBrief.class);
    }

    public DataInformation getMetadata(final String providerId, final String dataId, final String dataType) throws IOException {
        return client.get("data/metadata/"+providerId+"/"+dataId+"/"+dataType, MediaType.APPLICATION_XML_TYPE).getEntity(DataInformation.class);
    }

    public DefaultMetadata getMetadata(final String providerId) throws IOException {
        return client.get("provider/metadata/"+providerId, MediaType.APPLICATION_XML_TYPE).getEntity(DefaultMetadata.class);
    }

    public void saveMetadata(final String providerId, final DefaultMetadata metadata) throws IOException {
        client.post("provider/metadata/"+providerId, MediaType.APPLICATION_XML_TYPE, metadata);
    }
    
    public void reloadProvider(final String providerId) throws IOException {
        client.get("DP/" + providerId + "/restart", MediaType.APPLICATION_XML_TYPE);
    }
    
    public void restartAllLayerProviders() throws IOException {
        client.get("DP/restart", MediaType.APPLICATION_XML_TYPE);
    }
    
    public void restartAllStyleProviders() throws IOException {
        client.get("SP/restart", MediaType.APPLICATION_XML_TYPE);
    }
    
    public void deleteProvider(final String providerId, final boolean deleteData) throws IOException {
        client.delete("DP/" + providerId + "/" + Boolean.toString(deleteData), MediaType.APPLICATION_XML_TYPE);
    }
    
   
    public AcknowlegementType createProvider(final String serviceName, final ParameterValueGroup config) throws IOException {
        return client.post("DP/" + serviceName, MediaType.APPLICATION_XML_TYPE, config).getEntity(AcknowlegementType.class);
    }
    
    public AcknowlegementType updateProvider(final String serviceName, final String id, final ParameterValueGroup config) throws IOException {
        return client.put("DP/" + serviceName + "/" + id, MediaType.APPLICATION_XML_TYPE, config).getEntity(AcknowlegementType.class);
    }
    
    public GeneralParameterValue getProviderConfiguration(final String id, final ParameterDescriptorGroup descriptor) throws IOException, XMLStreamException {
        final Object object = client.get("DP/" + id + "/configuration", MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterValueReader reader = new ParameterValueReader(descriptor);
        reader.setInput(object);
        return reader.read();    
    }

    public ProvidersReport listProviders() throws IOException {
        return client.get("SP/providers", MediaType.APPLICATION_XML_TYPE).getEntity(ProvidersReport.class);  
    }
    
    public GeneralParameterDescriptor getServiceDescriptor(final String serviceName) throws IOException, XMLStreamException, ClassNotFoundException {
        
        final Object object = client.get("DP/service/descriptor/" + serviceName, MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterDescriptorReader reader = new ParameterDescriptorReader();
        reader.setInput(object);
        reader.read();
        Object response = reader.getDescriptorsRoot();
        if (response instanceof GeneralParameterDescriptor) {
            return (GeneralParameterDescriptor) response;
        }
        return null;
    }
 
    /**
     * Get the provider service source configuration description.
     *
     * @param serviceName name of the provider service.
     * @return
     */
    public GeneralParameterDescriptor getSourceDescriptor(final String serviceName) throws IOException, XMLStreamException, ClassNotFoundException {
        final Object object = client.get("DP/source/descriptor/" + serviceName, MediaType.APPLICATION_XML_TYPE).getEntity(Object.class);
        final ParameterDescriptorReader reader = new ParameterDescriptorReader();
        reader.setInput(object);
        reader.read();
        Object response = reader.getDescriptorsRoot();
        if (response instanceof GeneralParameterDescriptor) {
            return (GeneralParameterDescriptor) response;
        }
        return null;
    }
}
