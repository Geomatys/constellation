package org.constellation.engine.register.jooq.repository;

import static org.junit.Assert.*;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.DataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqDataRepositoryTestCase extends AbstractJooqTestTestCase {

    
    @Autowired
    private DataRepository dataRepository;
    @Test
    public void findAll() {
        dump(dataRepository.findAll());
    }

}
