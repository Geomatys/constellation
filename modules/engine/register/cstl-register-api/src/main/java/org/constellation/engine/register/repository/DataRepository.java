package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Data;

public interface DataRepository {

    List<Data> findAll();
    
    Data findByNameAndNamespaceAndProviderId(String name, String namespace, String providerIdentifier);

    Data fromLayer(String layerAlias, String providerId);

    Data save(Data data);
    
}
