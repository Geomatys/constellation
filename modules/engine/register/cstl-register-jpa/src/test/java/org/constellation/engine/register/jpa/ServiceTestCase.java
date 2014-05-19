/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.engine.register.jpa;

import java.lang.invoke.MethodHandles;
import java.util.List;

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
