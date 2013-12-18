package org.constellation.engine.register.jpa.repository;

import java.util.List;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.jpa.ServiceEntity;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, Integer>, ServiceRepository {
    @Query("select s from ServiceEntity s join s.layers l join fetch s.extraConfig ec join fetch s.metaData m where l.data.id = ?1")
    List<? extends Service> findByDataId(int dataId);

    @Query("select s from ServiceEntity s left join fetch s.layers l left join fetch s.extraConfig ec left join fetch s.metaData m where s.identifier = ?1 and s.type = ?2")
    Service findByIdentifierAndType(String id, String type);
    
    @Transactional
    void delete(Integer id);
    
    @Query("select s.identifier from ServiceEntity s where s.type = ?1")
    List<String> findIdentifiersByType(String type);
}
