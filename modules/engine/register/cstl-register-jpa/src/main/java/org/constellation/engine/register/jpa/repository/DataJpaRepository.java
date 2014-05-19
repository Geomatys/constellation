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
