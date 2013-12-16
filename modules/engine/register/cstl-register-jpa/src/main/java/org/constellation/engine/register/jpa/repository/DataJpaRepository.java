package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.DataEntity;
import org.constellation.engine.register.repository.DataRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataJpaRepository extends JpaRepository<DataEntity, Integer>, DataRepository {

}
