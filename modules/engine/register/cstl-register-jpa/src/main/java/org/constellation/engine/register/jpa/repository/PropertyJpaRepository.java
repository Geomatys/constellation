/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
