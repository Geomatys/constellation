package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.Provider;
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
    
    @Test
    public void byDomain() {
        dump(providerRepository.getProviderIdsForDomain(1));
    }
    
    @Test
    public void getProviderParentIdOfLayer() {
        Provider provider = providerRepository.getProviderParentIdOfLayer("WMS", "test","tile");
        if(provider==null)
            LOGGER.debug("Null parent id");
        else
            LOGGER.debug(provider.toString());
    }
}
