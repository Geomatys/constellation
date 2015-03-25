/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Map;

import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface MetadataRepository {
    
    int create(Metadata metadata);
    
    Metadata update(Metadata metadata);
    
    Metadata findByDataId(int dataId);

    Metadata findByDatasetId(int id);
    
    Metadata findByMetadataId(String metadataId);

    Metadata findById(int id);
    
    List<Metadata> findByCswId(Integer id);

    List<Metadata> findAll();

    List<Metadata> filterAndGet(final Map<String,Object> filterMap);

    Map<String,Integer> getProfilesCount();
    
    MetadataXCsw addMetadataToCSW(final String metadataID, final int cswID);
    
    void removeDataFromCSW(final String metadataID, final int cswID);
}
