package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.ProviderRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqProviderRepositoryTestCase extends AbstractJooqTestTestCase {
    
    @Autowired
    private ProviderRepository providerRepository;

    @Test
    public void all() {
        dump(providerRepository.findAll());
    }
}
