package org.constellation.engine.register.jpa;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.repository.DataRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-derby.xml")
public class DataTestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   

    @Autowired
    private DataRepository dataRepository;

   
    @Test
    @Transactional
    public void listData() {
        dump(dataRepository.findAll());
    }

    @Test
    @Transactional
    public void testFindByNameAndNamespaceAndProviderId() {
        Data data = dataRepository.findByNameAndNamespaceAndProviderId("repartition_albo_europe_2012", "http://geotoolkit.org", "1");
        dump(data);
    }    

    private void dump(Object o) {
        
        if(o!=null)
            LOGGER.debug(o.toString());
        
    }

    private void dump(List<?> findAll) {
        for (Object object : findAll) {
            LOGGER.debug(object.toString());
        }
    }

}
