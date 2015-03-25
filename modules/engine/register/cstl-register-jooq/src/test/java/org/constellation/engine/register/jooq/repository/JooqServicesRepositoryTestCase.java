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
package org.constellation.engine.register.jooq.repository;

import java.util.List;

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqServicesRepositoryTestCase extends AbstractJooqTestTestCase {

    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private DomainRepository domainRepository;
    
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
    public void findAccessibleServiceByType() {
        dump(serviceRepository.getAccessiblesServicesByType(1, "looo"));
    }
    
    @Test
    public void findByDomain() {
        dump(serviceRepository.findByDomain(1));
    }
    
    @Test
    public void save() {
    }

    @Test
    public void delete() {
    }

}
