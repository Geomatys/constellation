package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DomainJpaRepositoryTestCase extends AbstractRepositoryTestCase {

    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    private LayerRepository layerRepository;
    
    @Test
    public void findAll() {
        dump(domainRepository.findAll());
    }
    
    @Test
    @Transactional
    public void defaultDomain() {
        
        dump(layerRepository.findAll());
        
        Domain domain = domainRepository.findOne(1);
        dump(domain);
        dump(domain.getLayers());
        
    }
}
