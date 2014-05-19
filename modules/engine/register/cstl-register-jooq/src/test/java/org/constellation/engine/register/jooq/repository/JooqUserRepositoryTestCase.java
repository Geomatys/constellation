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
