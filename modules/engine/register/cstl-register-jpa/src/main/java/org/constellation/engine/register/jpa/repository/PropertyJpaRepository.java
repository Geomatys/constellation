package org.constellation.engine.register.jpa.repository;

import java.util.List;

import org.constellation.engine.register.Property;
import org.constellation.engine.register.jpa.PropertyEntity;
import org.constellation.engine.register.repository.PropertyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, String>, PropertyRepository {

	@Query("select p from PropertyEntity p where p.key IN ?1 ")
	List<PropertyEntity> findIn(List<String> keys);
	
	@Query("select p from PropertyEntity p where p.key LIKE ?1 ")
	List<PropertyEntity> startWith(String startWith);
	
	@Transactional
	void save(Property prop);

}
