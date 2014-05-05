package org.constellation.engine.register.jooq.repository;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.engine.register.User;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        BeanUtils.copyProperties(userDTO, user);
        dump(userDTO);
        
        userDTO.setFirstname("olivier");
        
        userDTO.addRole("cstl-admin");
        
        userRepository.insert(userDTO);
        
    }

    @Test
    public void delete() {
    }

}
