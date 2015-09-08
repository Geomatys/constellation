package org.constellation.database.api.repository;

import java.util.List;

import org.constellation.database.api.jooq.tables.pojos.StyledLayer;

/**
 *
 */
public interface StyledLayerRepository {
    List<StyledLayer> findByLayer(int layerId);
}
