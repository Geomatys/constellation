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
