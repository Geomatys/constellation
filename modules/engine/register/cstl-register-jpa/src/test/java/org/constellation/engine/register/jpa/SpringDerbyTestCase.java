package org.constellation.engine.register.jpa;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.TaskRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-derby.xml")
public class SpringDerbyTestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void listUser() {
        dump(userRepository.findAll());
    }

    @Test
    @Transactional
    public void listProvider() {
        dump(providerRepository.findAll());
        Provider provider = providerRepository.findOne(0);
        provider.getOwner();
    }

    @Test
    @Transactional
    public void listStyle() {
        dump(styleRepository.findAll());
    }

    @Test
    @Transactional
    public void listData() {
        dump(dataRepository.findAll());
    }

    @Test
    @Transactional
    public void listService() {
        dump(serviceRepository.findAll());
    }

    @Test
    @Transactional
    public void listLayer() {
        dump(layerRepository.findAll());
    }

    @Test
    @Transactional
    public void listTask() {
        dump(taskRepository.findAll());
    }
    
    @Test
    @Transactional
    public void saveUser() {
        UserEntity entity = new UserEntity();
        entity.setLogin("zoz");
        entity.setName("roro");
        entity.setPassword("ppp");
        userRepository.saveAndFlush(entity);
    }
    
   
    @Transactional
    public void deleteUser() {
        userRepository.delete("zozo");
    }
    

    private void dump(List<?> findAll) {
        for (Object object : findAll) {
            LOGGER.debug(object.toString());
        }
    }

}
