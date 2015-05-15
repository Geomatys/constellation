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

import org.constellation.engine.register.UserWithRole;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;

import com.google.common.base.Optional;


public interface UserRepository {

    List<CstlUser> findAll();

    CstlUser insert(CstlUser user);

    CstlUser update(CstlUser user);

    void addUserToRole(Integer userId, String roleName);
    
    int delete(int userId);
    
    int desactivate(int userId);
    
    int activate(int userId);
    

    boolean isLastAdmin(int userId);

    Optional<CstlUser> findOne(String login);

    Optional<CstlUser> findById(Integer id);

    Optional<CstlUser> findByEmail(String email);

    Optional<CstlUser> findByForgotPasswordUuid(String uuid);

    List<String> getRoles(int userId);
    
    

    int countUser();
    
    boolean loginAvailable(String login);


    Optional<UserWithRole> findOneWithRole(Integer id);

    Optional<UserWithRole> findOneWithRole(String name);

	List<UserWithRole> findActivesWithRole();

    List<UserWithRole> search(String search, int size, int page, String sortFieldName, String order);
    long searchCount(String search);
}
