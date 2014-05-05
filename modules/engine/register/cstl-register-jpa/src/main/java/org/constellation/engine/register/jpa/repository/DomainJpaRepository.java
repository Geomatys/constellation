package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.jpa.DomainEntity;
import org.constellation.engine.register.repository.DomainRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainJpaRepository extends JpaRepository<DomainEntity, Integer>, DomainRepository{

}
