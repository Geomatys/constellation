/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.ServiceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-derby.xml")
public class ServiceTestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    @Transactional
    public void listData() {
        dump(serviceRepository.findAll());
    }

    @Test
    @Transactional
    public void testFindByDataId() {
        List<? extends Service> services = serviceRepository.findByDataId(0);
        dump(services);
    }

    @Test
    @Transactional
    public void testFindByIdentifierAndType() {
        Service services = serviceRepository.findByIdentifierAndType("test", "WMS");
        dump(services);
    }

    @Test
    public void testFindIdentiersByType() {
        dump(serviceRepository.findIdentifiersByType("WMS"));
    }

    private void dump(List<?> findAll) {
        for (Object object : findAll) {
            LOGGER.debug(object.toString());
        }
    }

    private void dump(Object o) {
        if (o != null)
            LOGGER.debug(o.toString());
    }

}
