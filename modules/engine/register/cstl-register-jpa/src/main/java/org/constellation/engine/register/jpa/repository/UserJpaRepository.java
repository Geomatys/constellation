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

import java.util.List;

import org.constellation.engine.register.User;
import org.constellation.engine.register.jpa.UserEntity;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, UserRepository {

    @Transactional
    void save(User user);
    
    @Transactional
    void delete(String id);
    
    @Query("select u from UserEntity u left join fetch u.domains where u.login = :login")
    User findOneWithRolesAndDomains(@Param("login") String login);

    
    @Query("select u from UserEntity u left join fetch u.domains")
    List<? extends User> all();

    
}
