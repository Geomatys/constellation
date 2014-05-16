package org.constellation.engine.register.jooq;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public abstract class AbstractJooqTestTestCase {

    protected final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    DSLContext create;

   

    protected void dump(List<?> findAll) {
        for (Object property : findAll) {
                LOGGER.debug(property.toString());
        }

    }

    
    
    protected void dump(Result<Record> o) {
        if(o != null)
            LOGGER.debug(o.toString());
        
    }
    
    protected void dump(Object o) {
        if(o != null)
            LOGGER.debug(o.toString());
        
    }

}
