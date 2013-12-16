package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.ServiceEntity;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, Integer>, ServiceRepository {

}
