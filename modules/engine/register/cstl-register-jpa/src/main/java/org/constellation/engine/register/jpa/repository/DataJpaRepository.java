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

import org.constellation.engine.register.Data;
import org.constellation.engine.register.jpa.DataEntity;
import org.constellation.engine.register.repository.DataRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DataJpaRepository extends JpaRepository<DataEntity, Integer>, DataRepository {
    @Query("select d from DataEntity d join d.provider p where d.name= ?1 and d.namespace = ?2 and p.identifier = ?3")
    Data findByNameAndNamespaceAndProviderId(String name, String namespace, String providerIdentifier);
    
    @Query("select d from DataEntity d join d.provider p join d.layers l where l.alias= ?1 and d.namespace = ?2 and p.identifier = ?2")
    Data fromLayer(String layerAlias, String providerId);
    
}
