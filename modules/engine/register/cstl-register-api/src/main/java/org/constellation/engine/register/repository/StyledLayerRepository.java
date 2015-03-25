package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.StyledLayer;

/**
 *
 */
public interface StyledLayerRepository {
    List<StyledLayer> findByLayer(int layerId);
}
