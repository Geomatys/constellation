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
package org.constellation.engine.register.jooq.repository;

import java.util.List;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.repository.ServiceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqServicesRepositoryTestCase extends AbstractJooqTestTestCase {

    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Test
    public void all() {
        dump(serviceRepository.findAll());
    }
    
    @Test
    public void findByDataId() {
        List<Service> findByDataId = serviceRepository.findByDataId(3);
        dump(findByDataId);
    }
    
    @Test
    public void findByDataIdentierAndType() {
        Service service = serviceRepository.findByIdentifierAndType("test", "WMS");
        dump(service);
    }
    
    @Test
    public void findIdentifiersByType() {
        dump(serviceRepository.findIdentifiersByType("WMS"));
    }
    
    @Test
    public void save() {
    }

    @Test
    public void delete() {
    }

}
