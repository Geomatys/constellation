package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.StyleEntity;
import org.constellation.engine.register.repository.StyleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleJpaRepository extends JpaRepository<StyleEntity, Integer>, StyleRepository {

}
