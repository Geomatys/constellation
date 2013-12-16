package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.LayerEntity;
import org.constellation.engine.register.repository.LayerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LayerJpaRepository extends JpaRepository<LayerEntity, Integer>, LayerRepository {

}
