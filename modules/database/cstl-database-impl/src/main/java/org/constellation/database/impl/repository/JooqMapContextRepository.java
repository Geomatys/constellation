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
package org.constellation.database.impl.repository;


import static org.constellation.database.api.jooq.Tables.MAPCONTEXT;
import static org.constellation.database.api.jooq.Tables.MAPCONTEXT_STYLED_LAYER;

import java.util.List;

import org.constellation.database.api.jooq.tables.pojos.Mapcontext;
import org.constellation.database.api.jooq.tables.pojos.MapcontextStyledLayer;
import org.constellation.database.api.jooq.tables.records.MapcontextRecord;
import org.constellation.database.api.repository.MapContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqMapContextRepository extends AbstractJooqRespository<MapcontextRecord, Mapcontext> implements MapContextRepository {
    public JooqMapContextRepository() {
        super(Mapcontext.class, MAPCONTEXT);
    }

    @Override
    public Mapcontext findById(int id) {
        return dsl.select().from(MAPCONTEXT).where(MAPCONTEXT.ID.eq(id)).fetchOneInto(Mapcontext.class);
    }

    @Override
    public List<MapcontextStyledLayer> getLinkedLayers(int mapContextId) {
        return dsl.select().from(MAPCONTEXT_STYLED_LAYER)
                .where(MAPCONTEXT_STYLED_LAYER.MAPCONTEXT_ID.eq(mapContextId))
                .fetchInto(MapcontextStyledLayer.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void setLinkedLayers(int contextId, List<MapcontextStyledLayer> layers) {
        // Remove eventually existing old layers for this map context
        dsl.delete(MAPCONTEXT_STYLED_LAYER).where(MAPCONTEXT_STYLED_LAYER.MAPCONTEXT_ID.eq(contextId)).execute();

        if (layers.isEmpty()) {
            return;
        }

        for (final MapcontextStyledLayer layer : layers) {
            dsl.insertInto(MAPCONTEXT_STYLED_LAYER)
                .set(MAPCONTEXT_STYLED_LAYER.LAYER_ID, layer.getLayerId())
                .set(MAPCONTEXT_STYLED_LAYER.MAPCONTEXT_ID, layer.getMapcontextId())
                .set(MAPCONTEXT_STYLED_LAYER.STYLE_ID, layer.getStyleId())
                .set(MAPCONTEXT_STYLED_LAYER.LAYER_VISIBLE, layer.getLayerVisible())
                .set(MAPCONTEXT_STYLED_LAYER.LAYER_ORDER, layer.getLayerOrder())
                .set(MAPCONTEXT_STYLED_LAYER.LAYER_OPACITY, layer.getLayerOpacity())
                .set(MAPCONTEXT_STYLED_LAYER.EXTERNAL_LAYER, layer.getExternalLayer())
                .set(MAPCONTEXT_STYLED_LAYER.EXTERNAL_LAYER_EXTENT, layer.getExternalLayerExtent())
                .set(MAPCONTEXT_STYLED_LAYER.EXTERNAL_SERVICE_URL, layer.getExternalServiceUrl())
                .set(MAPCONTEXT_STYLED_LAYER.EXTERNAL_SERVICE_VERSION, layer.getExternalServiceVersion())
                .set(MAPCONTEXT_STYLED_LAYER.EXTERNAL_STYLE, layer.getExternalStyle())
                .set(MAPCONTEXT_STYLED_LAYER.ISWMS, layer.getIswms())
                .set(MAPCONTEXT_STYLED_LAYER.DATA_ID, layer.getDataId())
                .execute();
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Mapcontext create(Mapcontext mapContext) {
        MapcontextRecord newRecord = dsl.newRecord(MAPCONTEXT);
        newRecord.from(mapContext);
        newRecord.store();
        return newRecord.into(Mapcontext.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int update(Mapcontext mapContext) {
        return dsl.update(MAPCONTEXT)
                   .set(MAPCONTEXT.CRS, mapContext.getCrs())
                   .set(MAPCONTEXT.WEST, mapContext.getWest())
                   .set(MAPCONTEXT.SOUTH, mapContext.getSouth())
                   .set(MAPCONTEXT.EAST, mapContext.getEast())
                   .set(MAPCONTEXT.NORTH, mapContext.getNorth())
                   .set(MAPCONTEXT.DESCRIPTION, mapContext.getDescription())
                   .set(MAPCONTEXT.KEYWORDS, mapContext.getKeywords())
                   .set(MAPCONTEXT.NAME, mapContext.getName())
                   .set(MAPCONTEXT.OWNER, mapContext.getOwner())
                   .where(MAPCONTEXT.ID.eq(mapContext.getId())).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(int id) {
        return dsl.delete(MAPCONTEXT).where(MAPCONTEXT.ID.eq(id)).execute();
    }
}
