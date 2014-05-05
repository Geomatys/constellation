package org.constellation.engine.register.jpa;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-derby.xml")
public abstract class AbstractRepositoryTestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    void dump(Object o) {

        if (o != null)
            LOGGER.debug(o.toString());

    }

    void dump(List<?> findAll) {
        for (Object object : findAll) {
            dump(object);
        }
    }

}
