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
