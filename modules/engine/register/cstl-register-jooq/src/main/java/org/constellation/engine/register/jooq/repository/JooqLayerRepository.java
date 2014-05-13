package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.Layer;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.LayerRecord;
import org.constellation.engine.register.repository.LayerRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqLayerRepository extends AbstractJooqRespository<LayerRecord, Layer> implements LayerRepository {

    public JooqLayerRepository() {
        super(Layer.class, Tables.LAYER);
    }

}
