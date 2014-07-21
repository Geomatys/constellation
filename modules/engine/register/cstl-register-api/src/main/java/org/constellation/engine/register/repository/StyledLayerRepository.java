package org.constellation.engine.register.repository;

import org.constellation.engine.register.StyledLayer;

import java.util.List;

/**
 *
 */
public interface StyledLayerRepository {
    List<StyledLayer> findByLayer(int layerId);
}
