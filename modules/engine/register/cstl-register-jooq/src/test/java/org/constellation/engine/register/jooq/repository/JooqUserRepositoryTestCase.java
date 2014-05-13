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
