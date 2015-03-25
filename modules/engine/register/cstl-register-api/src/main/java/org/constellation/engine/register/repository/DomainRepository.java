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
package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Set;

import org.constellation.engine.register.jooq.tables.pojos.Domain;

public interface DomainRepository {
  
    List<Domain> findAll();
    
    List<Domain> findAllByUserId(int userId);

    Domain findOne(Integer id);
    
    Domain save(Domain domain);

    Domain update(Domain domain);

    int delete(int domainId);

    int[] addUserToDomain(int userId, int domainId, Set<Integer> roles);

    int removeUserFromDomain(int userId, int domainId);
    
    void addServiceToDomain(int serviceId, int domainId);
    
    int removeServiceFromDomain(int serviceId, int domainId);

    int removeUserFromAllDomain(int userId);

    int addDataToDomain(int dataId, int domainId);
    
    int removeDataFromDomain(int dataId, int domainId);

    int removeAllDataFromDomain(int i);

    int addProviderDataToDomain(String id, int activeDomainId);
    
 
    Domain findDefaultByUserId(Integer id);

 
    Set<Integer> updateUserInDomain(int userId, int domainId, Set<Integer> roles);

    List<Domain> findByIdsNotIn(List<Integer> fetch);

    List<Domain> findByIds(List<Integer> fetch);

    Set<Integer> findUserDomainIdsWithPermission(int userId, int permissionId);

    List<Domain> findByLinkedService(int serviceId);

    List<Domain> findByLinkedData(int dataId);

}
