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

import org.constellation.engine.register.jooq.AbstractJooqTestTestCase;
import org.constellation.engine.register.jooq.TestSamples;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JooqUserRepositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void all() {
        dump(userRepository.findAll());
    }

    @Test
    @Transactional()
    public void crude() throws Throwable {
        
        CstlUser insert = userRepository.insert(TestSamples.newAdminUser(), TestSamples.adminRoles());
        Assert.assertNotNull(insert);
        
        Assert.assertEquals("Should have deleled 1 record",1, userRepository.delete(insert.getId()));
        
    }

}
