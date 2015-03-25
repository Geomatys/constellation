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

import static org.constellation.engine.register.jooq.Tables.STYLED_LAYER;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.StyledLayer;
import org.constellation.engine.register.jooq.tables.records.StyledLayerRecord;
import org.constellation.engine.register.repository.StyledLayerRepository;
import org.springframework.stereotype.Component;


@Component
public class JooqStyledLayerRepository extends AbstractJooqRespository<StyledLayerRecord, StyledLayer> implements StyledLayerRepository {
    public JooqStyledLayerRepository() {
        super(StyledLayer.class, STYLED_LAYER);
    }

    @Override
    public List<StyledLayer> findByLayer(int layerId) {
        return dsl.select().from(STYLED_LAYER).where(STYLED_LAYER.LAYER.eq(layerId)).fetchInto(StyledLayer.class);
    }
}
