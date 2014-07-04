/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.Property;
import org.constellation.engine.register.jpa.PropertyEntity;
import org.constellation.engine.register.repository.PropertyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PropertyJpaRepository extends JpaRepository<PropertyEntity, String>, PropertyRepository {

	@Query("select p from PropertyEntity p where p.key IN ?1 ")
	List<PropertyEntity> findIn(List<String> keys);
	
	@Query("select p from PropertyEntity p where p.key LIKE ?1 ")
	List<PropertyEntity> startWith(String startWith);
	
	@Transactional
	void save(Property prop);

}
