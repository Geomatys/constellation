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

import org.constellation.configuration.DataBrief;
import org.constellation.configuration.StyleReport;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.dto.StyleListBean;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

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
    public StyleListBean getStyles() throws HttpResponseException, IOException {
        final String path = "SP/all/style/available";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBean.class);
    }

    /**
     * Queries the entire list of styles from the specified provider.
     *
     * @param providerId the style provider identifier
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleListBean getStyles(final String providerId) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);

        final String path = "SP/" + providerId + "/style/available";
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBean.class);
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
        client.delete(path, MediaType.APPLICATION_XML_TYPE, null).ensure2xxStatus();
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
    public void linkStyleToData(final String styleProvider, final String styleName, final String dataProvider, final String dataName) throws HttpResponseException, IOException {
        ensureNonNull("styleProvider", styleProvider);
        ensureNonNull("styleName", styleName);
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataName", dataName);

        final ParameterValues values = new ParameterValues();
        values.getValues().put("dataProvider", dataProvider);
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
    public void unlinkStyleFromData(final String styleProvider, final String styleName, final String dataProvider, final String dataName) throws HttpResponseException, IOException {
        ensureNonNull("styleProvider", styleProvider);
        ensureNonNull("styleName", styleName);
        ensureNonNull("dataProvider", dataProvider);
        ensureNonNull("dataName", dataName);

        final ParameterValues values = new ParameterValues();
        values.getValues().put("dataProvider", dataProvider);
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

    public DataInformation loadData(final String filePath, final String dataType) throws IOException {
        ParameterValues pv = new ParameterValues();
        HashMap<String, String> parameters = new HashMap<>(0);
        parameters.put("filePath", filePath);
        parameters.put("dataType", dataType);
        pv.setValues(parameters);


        return client.post("data/load", MediaType.APPLICATION_XML_TYPE, pv).getEntity(DataInformation.class);
    }

    /**
     *
     * @param locale
     * @return
     * @throws IOException
     */
    public MetadataLists getMetadataCodeLists(final String locale) throws IOException {
        return client.get("data/metadataCodeLists/" + locale, MediaType.APPLICATION_XML_TYPE).getEntity(MetadataLists.class);
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

    /**
     * Ask pyramid data folder
     * @param providerName provider name to found pyramid folder
     * @return path as String
     * @throws IOException
     */
    public String getPyramidPath(final String providerName) throws IOException {
        SimpleValue sentValue = client.get("data/pyramid/"+providerName+"/folder", MediaType.APPLICATION_XML_TYPE).getEntity(SimpleValue.class);
        return sentValue.getValue();
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

    public DataBrief getDataSummary(final QName name, final String providerId) throws IOException {
        return client.get("data/summary/"+providerId+"/"+name.getLocalPart()+"/"+name.getNamespaceURI(), MediaType.APPLICATION_XML_TYPE).getEntity(DataBrief.class);
    }

    public DataBrief getLayerSummary(final String layerAlias, final String providerId) throws IOException {
        return client.get("data/layer/summary/"+providerId+"/"+layerAlias, MediaType.APPLICATION_XML_TYPE).getEntity(DataBrief.class);
    }

    public DataInformation getMetadata(final String providerId, final String dataId, final String dataType) throws IOException {
        return client.get("data/metadata/"+providerId+"/"+dataId+"/"+dataType, MediaType.APPLICATION_XML_TYPE).getEntity(DataInformation.class);
    }
}
