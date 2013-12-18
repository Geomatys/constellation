package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.jpa.DataEntity;
import org.constellation.engine.register.repository.DataRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DataJpaRepository extends JpaRepository<DataEntity, Integer>, DataRepository {
    @Query("select d from DataEntity d join d.provider p where d.name= ?1 and d.namespace = ?2 and p.identifier = ?3")
    Data findByNameAndNamespaceAndProviderId(String name, String namespace, String providerIdentifier);
    
    @Query("select d from DataEntity d join d.provider p join d.layers l where l.alias= ?1 and d.namespace = ?2 and p.identifier = ?2")
    Data fromLayer(String layerAlias, String providerId);
    
}
