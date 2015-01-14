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
package org.constellation.business;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerSummary;
import org.constellation.dto.AddLayer;

import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface ILayerBusiness {
    void removeAll();

    void add(AddLayer layer) throws ConfigurationException;

    void add(String name, String namespace, String providerId, String alias,
             String serviceId, String serviceType, org.constellation.configuration.Layer config) throws ConfigurationException;

    void updateLayerTitle(LayerSummary layer) throws ConfigurationException;

    void removeForService(String serviceName, String identifier) throws ConfigurationException;

    List<Layer> getLayers(String serviceType, String serviceName, String userLogin) throws ConfigurationException;

    Layer getLayer(String spec, String identifier, String name, String namespace, String login) throws ConfigurationException;

    void remove(String spec, String serviceId, String layerId, String namespace) throws ConfigurationException;

    List<org.constellation.engine.register.Layer> findByStyleId(final Integer styleId);

    List<LayerSummary> getLayerSummaryFromStyleId(final Integer styleId);

}
