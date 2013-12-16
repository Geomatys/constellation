package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Data;

public interface DataRepository {

    List<? extends Data> findAll();
    
}
