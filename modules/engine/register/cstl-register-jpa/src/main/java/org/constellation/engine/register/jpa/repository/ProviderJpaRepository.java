package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.ProviderEntity;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderJpaRepository extends JpaRepository<ProviderEntity, Integer>, ProviderRepository {

    
}
