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
package org.constellation.engine.register.jooq.repository;


import org.constellation.engine.register.Mapcontext;
import org.constellation.engine.register.MapcontextStyledLayer;
import org.constellation.engine.register.helper.MapcontextHelper;
import org.constellation.engine.register.jooq.tables.records.MapcontextRecord;
import org.constellation.engine.register.repository.MapContextRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.constellation.engine.register.jooq.Tables.MAPCONTEXT;
import static org.constellation.engine.register.jooq.Tables.MAPCONTEXT_STYLED_LAYER;

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
               .set(MAPCONTEXT_STYLED_LAYER.LAYER_VISIBLE, layer.isLayerVisible())
               .set(MAPCONTEXT_STYLED_LAYER.LAYER_ORDER, layer.getLayerOrder())
               .set(MAPCONTEXT_STYLED_LAYER.LAYER_OPACITY, layer.getLayerOpacity())
               .execute();
        }
    }

    @Override
    public Mapcontext create(Mapcontext mapContext) {
        MapcontextRecord newRecord = MapcontextHelper.copy(mapContext, dsl.newRecord(MAPCONTEXT));

        newRecord.store();
        return newRecord.into(Mapcontext.class);
    }

    @Override
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
    public int delete(int id) {
        return dsl.delete(MAPCONTEXT).where(MAPCONTEXT.ID.eq(id)).execute();
    }
}
