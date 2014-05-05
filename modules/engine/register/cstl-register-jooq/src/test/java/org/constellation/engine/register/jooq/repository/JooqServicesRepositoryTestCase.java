package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.ServiceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqServicesRepositoryTestCase extends AbstractJooqTestTestCase {

    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Test
    public void all() {
        dump(serviceRepository.findAll());
        
        
    }
    
    @Test
    public void getValue() {
      }
    
    @Test
    public void save() {
    }

    @Test
    public void delete() {
    }

}
