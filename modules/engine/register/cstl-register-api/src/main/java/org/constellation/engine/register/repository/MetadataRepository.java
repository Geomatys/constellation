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
    
    List<Metadata> findAll(final boolean includeService, final boolean onlyPublished);
    
    List<String> findAllIsoMetadata(final boolean includeService, final boolean onlyPublished);
            
    List<MetadataBbox> getBboxes(int id);
    
    int delete(int id);

    void deleteAll();
    
    List<Metadata> findByCswId(Integer id);
    
    List<String> findMetadataIDByCswId(final Integer id, final boolean includeService, final boolean onlyPublished);
    
    int countMetadataByCswId(final Integer id, final boolean includeService, final boolean onlyPublished);

    List<String> findMetadataID(final boolean includeService, final boolean onlyPublished);
    
    int countMetadata(final boolean includeService, final boolean onlyPublished);
    
    boolean isLinkedMetadata(Integer metadataID, Integer cswID);
    
    boolean isLinkedMetadata(String metadataID, String cswID);
    
    boolean isLinkedMetadata(String metadataID, String cswID, final boolean includeService, final boolean onlyPublished);

    List<Metadata> findAll();
    
    Map<Integer, List> filterAndGet(final Map<String,Object> filterMap, final Map.Entry<String,String> sortEntry,final int pageNumber,final int rowsPerPage);

    Map<Integer,String> filterAndGetWithoutPagination(final Map<String,Object> filterMap);

    Map<String,Integer> getProfilesCount(final Map<String,Object> filterMap);
    
    MetadataXCsw addMetadataToCSW(final String metadataID, final int cswID);
    
    void removeDataFromCSW(final String metadataID, final int cswID);
    
    void changeOwner(final int id, final int owner);
    
    void changeValidation(final int id, final boolean validated);
    
    void changePublication(final int id, final boolean published);
    
    void changeProfile(final int id, final String newProfile);
    
    int countTotalMetadata(final Map<String,Object> filterMap);
    
    int countValidated(final boolean status,final Map<String,Object> filterMap);
    
    int countPublished(final boolean status,final Map<String,Object> filterMap);
    
    int countInCompletionRange(final Map<String,Object> filterMap, final int minCompletion, final int maxCompletion);
    
    void setValidationRequired(final int id, final String state, final String validationState);
    
    void denyValidation(final int id, final String comment);
    
    boolean existInternalMetadata(final String metadataID, final boolean includeService, final boolean onlyPublished);
}
