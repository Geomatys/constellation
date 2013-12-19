package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Style;

public interface StyleRepository {

    List<? extends Style> findAll();
    
}
