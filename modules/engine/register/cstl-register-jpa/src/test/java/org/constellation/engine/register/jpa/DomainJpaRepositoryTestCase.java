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

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DomainJpaRepositoryTestCase extends AbstractRepositoryTestCase {

    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    private LayerRepository layerRepository;
    
    @Test
    public void findAll() {
        dump(domainRepository.findAll());
    }
    
    @Test
    @Transactional
    public void defaultDomain() {
        
        dump(layerRepository.findAll());
        
        Domain domain = domainRepository.findOne(1);
        dump(domain);
        dump(domain.getLayers());
        
    }
}
