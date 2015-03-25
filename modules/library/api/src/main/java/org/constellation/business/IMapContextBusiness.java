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

import java.util.List;

import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.dto.ParameterValues;
import org.constellation.engine.register.jooq.tables.pojos.Mapcontext;
import org.constellation.engine.register.jooq.tables.pojos.MapcontextStyledLayer;
import org.opengis.util.FactoryException;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IMapContextBusiness {
    List<MapContextLayersDTO> findAllMapContextLayers();

    Mapcontext create(final MapContextLayersDTO mapContext);

    void setMapItems(final int contextId, final List<MapcontextStyledLayer> layers);

    MapContextLayersDTO findMapContextLayers(int contextId);

    String findStyleName(Integer styleId);

    ParameterValues getExtent(int contextId) throws FactoryException;

    ParameterValues getExtentForLayers(final List<MapcontextStyledLayer> styledLayers) throws FactoryException;
}
