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
package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.configuration.LayerSummary;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Layer;
import org.constellation.engine.register.jooq.tables.pojos.Service;

public interface LayerRepository {

    List<Layer> findAll();

    Layer findById(Integer layerId);
    
    List<Layer> findByServiceId(int serviceId);
    
    List<Layer> findByDataId(int dataId);

    /**
     * Delete all layer associated to a service.
     * @param service service of which layers are removed.
     * @return number of layer removed.
     */
    int deleteServiceLayer(Service service);

    Layer save(Layer storeLayer);

    /**
     * Update a layer entry.
     * @param storeLayer layer to update
     */
    void update(Layer storeLayer);

    void delete(int layerId);
    
    Layer findByServiceIdAndLayerName(int serviceId, String layerName);
    
    Layer findByServiceIdAndLayerName(int serviceId, String layerName, String namespace);

    Data findDatasFromLayerAlias(String layerAlias, String dataProviderIdentifier);

    void updateLayerTitle(LayerSummary layerId);

    /**
     * Retrieve all layers linked to a given style id.
     *
     * @param styleId style id candidate
     * @return list of {@link Layer}
     */
    List<Layer> getLayersByLinkedStyle(final int styleId);

    /**
     * Retrieve all layer reference linked to a given style id.
     * Output Layer object only contain fields :
     * <ul>
     *     <li>id</li>
     *     <li>name</li>
     * </ul>
     * @param styleId style id candidate
     * @return list of lighten {@link Layer}
     */
    List<Layer> getLayersRefsByLinkedStyle(final int styleId);

    int getDataSet(String layer);
}
