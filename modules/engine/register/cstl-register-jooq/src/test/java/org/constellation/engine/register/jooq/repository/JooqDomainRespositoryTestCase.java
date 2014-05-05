package org.constellation.engine.register.jooq.repository;

import java.util.HashSet;
import java.util.Set;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.DomainRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqDomainRespositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private DomainRepository domainRepository;
    
    @Test
    public void all() {
        dump(domainRepository.findAll());
    }
    
    @Test
    public void save() {
        Domain domainDTO = new Domain("cadastre", "Domaine du cadastre");
        Domain save = domainRepository.save(domainDTO);
        LOGGER.debug("New domains: " + domainDTO);
    }
    
    @Test
    public void update() {
        Domain domainDTO = new Domain(3, "cadastre mec", "Domaine du cadastre");
        domainRepository.update(domainDTO);
        LOGGER.debug("New domains: " + domainDTO);
        
    }
    
    @Test
    public void delete() {
        int n = domainRepository.delete(3);
        LOGGER.debug("Delete " + n + " domains");
    }
    
    @Test
    public void testAddUserToDomain() {
        Set<String> roles = new HashSet<String>(); 
        roles.add("manager");
        domainRepository.addUserToDomain("olivier", 2, roles );
    }
    
    @Test
    public void testRemoveUserFromDomain() {
        int removeUserFromDomain = domainRepository.removeUserFromDomain("zozoz", 1);
        LOGGER.debug("Removed: " + removeUserFromDomain);
    }

}
