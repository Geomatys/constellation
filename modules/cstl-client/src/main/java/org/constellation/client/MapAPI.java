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
import org.constellation.ServiceDef;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AddLayer;

/**
 *
 */
public class MapAPI {

    /**
     * Client used to communicate with the Constellation server.
     */
    private final ConstellationClient client;

    /**
     *
     * @param client the client to use
     */
    MapAPI(final ConstellationClient client) {
        this.client = client;
    }
    
    /**
     * path : /1/MAP/{spec}/{id}/layersummary/all<br>
     * method : GET<br>
     * java : org.constellation.rest.api.MapRest.getLayersSummary<br>
     */
    public void getLayersSummary(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/MAP/{spec}/{id}/layer<br>
     * method : PUT<br>
     * java : org.constellation.rest.api.MapRest.addLayer<br>
     * <br>
     * Adds a new layer to a "map" service instance.
     * <p>
     * Only for "map" services: WMS, WMTS, WCS, WFS.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @param layer the layer to be added
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error
     */
    public void addLayer(final ServiceDef.Specification serviceType, final String identifier, final AddLayer layer) throws HttpResponseException, IOException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("layer", layer);

        final String path = "MAP/" + serviceType + "/" + identifier + "/layer";
        client.put(path, MediaType.APPLICATION_XML_TYPE, layer).ensure2xxStatus();
    }

    /**
     * path : /1/MAP/{spec}/{id}/layer/update/title<br>
     * method : POST<br>
     * java : org.constellation.rest.api.MapRest.updateLayerTitle<br>
     */
    public void updateLayerTitle(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/MAP/{spec}/{id}/delete/{layerid}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.MapRest.deleteLayer<br>
     * <br>
     * delete service layer
     * 
     * @param serviceId service identifier
     * @param layerId data layer name
     * @param layerNamespace
     * @param spec service specification
     * @throws IOException
     */
    public void deleteLayer(final String serviceId, final String layerId, final String layerNamespace, final String spec) throws IOException {
        client.delete("MAP/" + spec + "/" + serviceId+"/delete/"+layerId, MediaType.APPLICATION_XML_TYPE, "layernamespace", layerNamespace).ensure2xxStatus();
    }

    /**
     * path : /1/MAP/{spec}/{id}/updatestyle<br>
     * method : POST<br>
     * java : org.constellation.rest.api.MapRest.updateLayerStyleForService<br>
     */
    public void updateLayerStyleForService(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/MAP/{spec}/{id}/removestyle<br>
     * method : POST<br>
     * java : org.constellation.rest.api.MapRest.removeLayerStyleForService<br>
     */
    public void removeLayerStyleForService(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/MAP/{spec}/{id}/extractLayerInfo/{layerName}/{crs}<br>
     * method : POST<br>
     * java : org.constellation.rest.api.MapRest.extractWMTSLayerInfo<br>
     */
    public void extractWMTSLayerInfo(){
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * path : /1/MAP/{spec}/{id}/layer/all<br>
     * method : GET<br>
     * java : org.constellation.rest.api.MapRest.getLayers<br>
     * <br>
     * Queries the layer list of a "map" service instance.
     * <p>
     * Only for "map" services: WMS, WMTS, WCS, WFS.
     *
     * @param serviceType the service type
     * @param identifier  the service identifier
     * @return a {@link LayerList} instance
     * @throws HttpResponseException if the response does not have a {@code 2xx} status code
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public LayerList getLayers(final ServiceDef.Specification serviceType, final String identifier) throws HttpResponseException, IOException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("identifier",  identifier);

        final String path = "MAP/" + serviceType + "/" + identifier + "/layer/all";
        return (LayerList) client.get(path, MediaType.APPLICATION_XML_TYPE).getEntity(LayerList.class);
    }


}
