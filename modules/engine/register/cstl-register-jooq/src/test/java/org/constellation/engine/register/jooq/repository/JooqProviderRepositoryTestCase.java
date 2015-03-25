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
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.repository.ProviderRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqProviderRepositoryTestCase extends AbstractJooqTestTestCase {
    
    @Autowired
    private ProviderRepository providerRepository;

    @Test
    public void all() {
        dump(providerRepository.findAll());
    }
    
    @Test
    public void byDomain() {
        dump(providerRepository.getProviderIdsForDomain(1));
    }
    
    @Test
    public void getProviderParentIdOfLayer() {
        Provider provider = providerRepository.getProviderParentIdOfLayer("WMS", "test","tile");
        if(provider==null)
            LOGGER.debug("Null parent id");
        else
            LOGGER.debug(provider.toString());
    }
}
