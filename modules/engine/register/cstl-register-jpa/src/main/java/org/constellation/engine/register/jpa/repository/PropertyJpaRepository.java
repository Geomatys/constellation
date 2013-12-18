package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.PropertyEntity;
import org.constellation.engine.register.repository.PropertyRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, String>, PropertyRepository {

    
    
}
