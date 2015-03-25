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
package org.constellation.admin.dto;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.Mapcontext;

public class MapContextLayersDTO extends Mapcontext {
    private List<MapContextStyledLayerDTO> layers;

    private String userOwner;

    public MapContextLayersDTO() {
        super();
    }

    public MapContextLayersDTO(final Mapcontext mapContext, final List<MapContextStyledLayerDTO> layers) {
        super(mapContext.getId(), mapContext.getName(), mapContext.getOwner(), mapContext.getDescription(), mapContext.getCrs(),
              mapContext.getWest(), mapContext.getNorth(), mapContext.getEast(), mapContext.getSouth(), mapContext.getKeywords());
        this.layers = layers;
    }

    public List<MapContextStyledLayerDTO> getLayers() {
        return layers;
    }

    public void setLayers(List<MapContextStyledLayerDTO> layers) {
        this.layers = layers;
    }

    public String getUserOwner() {
        return userOwner;
    }

    public void setUserOwner(String userOwner) {
        this.userOwner = userOwner;
    }
}
