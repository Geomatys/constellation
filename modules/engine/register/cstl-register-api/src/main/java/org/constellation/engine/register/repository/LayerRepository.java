package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Layer;

public interface LayerRepository {

    List<? extends Layer> findAll();
    
}
