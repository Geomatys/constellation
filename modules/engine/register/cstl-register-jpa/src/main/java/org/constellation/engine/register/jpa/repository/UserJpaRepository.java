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

import org.constellation.engine.register.User;
import org.constellation.engine.register.jpa.UserEntity;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, UserRepository {

    @Transactional("txManager")
    void save(User user);
    
    @Transactional("txManager")
    void delete(String id);
    
    @Query("select u from UserEntity u left join fetch u.domains where u.login = :login")
    User findOneWithRolesAndDomains(@Param("login") String login);

    
    @Query("select u from UserEntity u left join fetch u.domains")
    List<? extends User> all();

    
}
