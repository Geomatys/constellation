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

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.jpa.ServiceEntity;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, Integer>, ServiceRepository {
    @Query("select s from ServiceEntity s join s.layers l join fetch s.extraConfig ec join fetch s.metaDatas m where l.data.id = ?1")
    List<? extends Service> findByDataId(int dataId);

    @Query("select s from ServiceEntity s left join fetch s.layers l left join fetch s.extraConfig ec left join fetch s.metaDatas m where s.identifier = ?1 and s.type = ?2")
    Service findByIdentifierAndType(String id, String type);

    @Query("select m from ServiceEntity s join s.metaDatas m where s.identifier = ?1 and s.type = ?2 and m.lang = ?3")    
    ServiceMetaData findMetaDataForLangByIdentifierAndType(String identifier, String serviceType, String language);

    @Transactional
    void delete(Integer id);
    
    @Query("select s.identifier from ServiceEntity s where s.type = ?1")
    List<String> findIdentifiersByType(String type);
}
