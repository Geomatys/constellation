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
