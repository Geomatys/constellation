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
package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Set;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.User;

public interface DomainRepository {
  
    List<Domain> findAll();

    Domain findOne(Integer id);
    
    Domain save(Domain domain);

    void update(Domain domain);

    int delete(int domainId);

    int[] addUserToDomain(String userId, int domainId, Set<String> roles);

    int removeUserFromDomain(String userId, int domainId);
    
    void addServiceToDomain(int serviceId, int domainId);
    
    int removeServiceFromDomain(int serviceId, int domainId);

    int removeUserFromAllDomain(String userId);

    int addDataToDomain(int dataId, int domainId);
    
    int removeDataFromDomain(int dataId, int domainId);

    int removeAllDataFromDomain(int i);

    int addProviderDataToDomain(String id, int activeDomainId);

    List<User> findUsers(int id);


}
