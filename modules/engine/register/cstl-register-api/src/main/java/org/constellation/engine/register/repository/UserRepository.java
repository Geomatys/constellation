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
import java.util.Map;

import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;

import com.google.common.base.Optional;


public interface UserRepository {

    List<CstlUser> findAll();
    
    List<DomainUser> findAllWithDomainAndRole();
    
    List<CstlUser> findUsersByDomainId(int domainId);
    
    CstlUser insert(CstlUser user, List<String> roles);

    /**
     * Update user, should not update password.
     * @param user
     * @param roles
     * @return
     */
    CstlUser update(CstlUser user, List<String> roles);
    
    int delete(int userId);
    
    int desactivate(int userId);
    
    int activate(int userId);
    
    Optional<DomainUser> findOneWithRolesAndDomains(String login);
    
    Optional<DomainUser> findOneWithRolesAndDomains(int id);

    boolean isLastAdmin(int userId);

    Optional<CstlUser> findOne(String login);

    Optional<CstlUser> findById(Integer id);

    List<String> getRoles(int userId);
    
    

    int countUser();
    
    boolean loginAvailable(String login);

    Map<CstlUser, List<Domainrole>> findUsersWithDomainRoles(int domainId);

    List<CstlUser> findUsersNotInDomain(int domainId);
    
}
