package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.DomainroleRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqDomainRoleRepositoryTestCase extends AbstractJooqTestTestCase {
    
    
    @Autowired
    private DomainroleRepository domainRoleRepository;

    @Test
    public void all() {
        dump(domainRoleRepository.findAll());
    }
    
    
    @Test
    public void allWithMember() {
        dump(domainRoleRepository.findAllWithMembers());
    }
}
