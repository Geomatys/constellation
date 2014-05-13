package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.LayerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqLayerRepositoryTestCase extends AbstractJooqTestTestCase {
    
    @Autowired
    private LayerRepository layerRepository;

    @Test
    public void all() {
        dump(layerRepository.findAll());
    }
}
