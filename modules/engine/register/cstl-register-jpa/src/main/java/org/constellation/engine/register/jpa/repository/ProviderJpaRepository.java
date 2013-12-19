package org.constellation.engine.register.jpa.repository;

import java.util.List;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.jpa.ProviderEntity;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderJpaRepository extends JpaRepository<ProviderEntity, Integer>, ProviderRepository {

    List<? extends Provider> findByImpl(String serviceName);
}
