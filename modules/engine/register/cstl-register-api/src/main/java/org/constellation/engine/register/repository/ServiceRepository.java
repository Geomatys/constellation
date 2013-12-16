package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Service;

public interface ServiceRepository {

    List<? extends Service> findAll();
    
}
