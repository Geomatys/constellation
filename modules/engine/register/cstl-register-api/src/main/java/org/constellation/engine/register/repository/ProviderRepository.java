package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Provider;

 public interface ProviderRepository {

    List<Provider> findAll();

    Provider findOne(Integer id);

    List<Provider> findByImpl(String serviceName);

    List<String> getProviderIds();

    Provider findByIdentifie(String providerIdentifier);


    
}
