package org.constellation.engine.register.jooq.repository;

import java.util.List;

import org.constellation.engine.register.Service;
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
    public void findByDataId() {
        List<Service> findByDataId = serviceRepository.findByDataId(3);
        dump(findByDataId);
    }
    
    @Test
    public void findByDataIdentierAndType() {
        Service service = serviceRepository.findByIdentifierAndType("test", "WMS");
        dump(service);
    }
    
    @Test
    public void findIdentifiersByType() {
        dump(serviceRepository.findIdentifiersByType("WMS"));
    }
    
    @Test
    public void save() {
    }

    @Test
    public void delete() {
    }

}
