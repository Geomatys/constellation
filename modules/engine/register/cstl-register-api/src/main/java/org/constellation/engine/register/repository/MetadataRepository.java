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
import org.constellation.engine.register.MetadataComplete;

import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.MetadataBbox;
import org.constellation.engine.register.jooq.tables.pojos.MetadataXCsw;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface MetadataRepository {
    
    int create(MetadataComplete metadata);
    
    Metadata update(MetadataComplete metadata);
    
    Metadata findByDataId(int dataId);

    Metadata findByDatasetId(int id);
    
    Metadata findByMetadataId(String metadataId);

    Metadata findById(int id);
    
    List<MetadataBbox> getBboxes(int id);
    
    int delete(int id);
    
    List<Metadata> findByCswId(Integer id);
    
    boolean isLinkedMetadata(Integer metadataID, Integer cswID);

    List<Metadata> findAll();

    Map<Integer, List> filterAndGet(final Map<String,Object> filterMap, final Map.Entry<String,String> sortEntry,final int pageNumber,final int rowsPerPage);

    Map<Integer,String> filterAndGetWithoutPagination(final Map<String,Object> filterMap);

    Map<String,Integer> getProfilesCount();
    
    MetadataXCsw addMetadataToCSW(final String metadataID, final int cswID);
    
    void removeDataFromCSW(final String metadataID, final int cswID);
    
    void changeOwner(final int id, final int owner);
    
    void changeValidation(final int id, final boolean validated);
    
    void changePublication(final int id, final boolean published);
    
    void changeProfile(final int id, final String newProfile);
    
    int countTotalMetadata();
    
    int countValidated(final boolean status);
    
    int countPublished(final boolean status);
}
