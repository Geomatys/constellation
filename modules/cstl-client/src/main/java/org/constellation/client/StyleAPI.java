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
import org.constellation.configuration.StyleReport;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.StyleListBrief;
import org.geotoolkit.style.MutableStyle;

/**
 *
 */
public class StyleAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    StyleAPI(final ConstellationClient client) {
        this.client = client;
    }

    /**
     * path : /1/SP/{id}/style/{styleId}/{ruleName}/{interval}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.getPaletteStyle<br>
     */
    public void getPaletteStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/generateAutoInterval<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.generateAutoIntervalStyle<br>
     */
    public void generateAutoIntervalStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/generateAutoUnique<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.generateAutoUniqueStyle<br>
     */
    public void generateAutoUniqueStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/available<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.getAvailableStyles<br>
     * <br>
     * Queries the entire list of styles from the specified provider.
     *
     * @param providerId the style provider identifier
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleListBrief getAvailableStyles(final String providerId, final String category) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        String path = "SP/" + providerId + "/style/available";
        if(category != null && !category.isEmpty()){
            path += "/"+category;
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBrief.class);
    }

    /**
     * path : /1/SP/all/style/available/{category}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.getCategoryAvailableStyles<br>
     * <br>
     * Queries the entire list of styles.
     *
     * @return the list of available styles
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public StyleListBrief getCategoryAvailableStyles(final String category) throws HttpResponseException, IOException {
        String path = "SP/all/style/available";
        if(category != null && !category.isEmpty()){
            path += "/"+category;
        }
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(StyleListBrief.class);
    }

    /**
     * path : /1/SP/{id}/style/{styleId}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.updateStyle<br>
     */
    public void updateStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/{styleId}/update<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.StyleRest.updateStyleJson<br>
     */
    public void updateStyleJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/{styleId}<br>
     * method : DELETE<br>
     * java : org.constellation.rest.api.StyleRest.deleteStyle<br>
     * <br>
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
     * path : /1/SP/{id}/style/{styleId}/report<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.getStyleReport<br>
     * <br>
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
     * path : /1/SP/{id}/style/{styleId}/linkData<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.linkToData<br>
     * <br>
     * Links a style resource to an existing data resource.
     *
     * @param styleProvider the style provider identifier
     * @param styleName     the style name
     * @param dataProvider  the data provider identifier
     * @param dataName      the data name
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException           on HTTP communication error or response entity parsing error
     */
    public void linkToData(final String styleProvider, final String styleName, final String dataProvider, final String dataName, final String namespace) throws HttpResponseException, IOException {
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
     * path : /1/SP/{id}/style/{styleId}/unlinkData<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.unlinkFromData<br>
     * <br>
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
     * path : /1/SP/{id}/style/{styleId}/import<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.importStyle<br>
     */
    public void importStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/{styleId}/export<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.exportStyle<br>
     */
    public void exportStyle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/getChartDataJson<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.getChartDataJson<br>
     */
    public void getChartDataJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/statistics<br>
     * method : POST<br>
     * java : org.constellation.rest.api.StyleRest.getHistogram<br>
     */
    public void getHistogram(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style/{styleId}<br>
     * method : GET<br>
     * java : org.constellation.rest.api.StyleRest.getStyle<br>
     */
    public MutableStyle getStyle(final String providerId, final String styleName) throws HttpResponseException, IOException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("styleName", styleName);

        final String path = "SP/" + providerId + "/style/" + styleName;
        return client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(MutableStyle.class);
    }

    /**
     * path : /1/SP/{id}/style/create<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.StyleRest.createStyleJson<br>
     */
    public void createStyleJson(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/SP/{id}/style<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.StyleRest.createStyle<br>
     */
    public void createStyle(final String providerId, final MutableStyle style) throws IOException {
        final String path = "SP/" + providerId + "/style";
        client.put(path, MediaType.APPLICATION_XML_TYPE, style);
    }


}
