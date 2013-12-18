package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Provider;

 public interface ProviderRepository {

    List<? extends Provider> findAll();

    Provider findOne(Integer id);

    List<? extends Provider> findByImpl(String serviceName);


    
}
