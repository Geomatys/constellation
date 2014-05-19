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
package org.constellation.engine.register.jooq.repository;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.engine.register.User;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JooqUserRepositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void all() {
        dump(userRepository.all());
    }

    @Test
    public void insert() throws Throwable {
        User user = userRepository.findOneWithRolesAndDomains("olivier");

        User userDTO = new User();
        if (user == null) {
            userDTO.setFirstname("olivier");
            userDTO.setLastname("Nouguier");
            userDTO.setLogin("olivier");
            userDTO.setEmail("olvier.nouguier@gmail.com");
            userDTO.setPassword("zozozozo");
            userDTO.addRole("cstl-admin");
        } else
            BeanUtils.copyProperties(userDTO, user);

        userDTO.setFirstname("olivier");

        userDTO.addRole("cstl-admin");

        userRepository.insert(userDTO);

    }

    @Test
    public void delete() {
        userRepository.delete("olivier");
    }

}
