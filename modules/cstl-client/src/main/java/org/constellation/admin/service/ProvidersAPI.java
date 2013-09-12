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

import org.constellation.dto.DataInformation;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.StyleListBean;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

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
     * @throws IOException on HTTP communication error or response entity parsing error
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
     * @param path
     * @return
     * @throws IOException
     */
    public List<FileBean> getDataFolder(String path) throws IOException {
        ensureNonNull("path", path);
        if(path.isEmpty()){
            path = "root";
        }
        final FileListBean list = client.post("data/datapath", MediaType.APPLICATION_XML_TYPE, path).getEntity(FileListBean.class);
        return list.getList();
    }

    public DataInformation loadData(final String filePath, final String name, final String dataType) throws IOException {
        ParameterValues pv = new ParameterValues();
        HashMap<String, String> parameters = new HashMap<>(0);
        parameters.put("filePath", filePath);
        parameters.put("name", name);
        parameters.put("dataType", dataType);
        pv.setValues(parameters);


        return client.post("data/load", MediaType.APPLICATION_XML_TYPE, pv).getEntity(DataInformation.class);
    }
}
