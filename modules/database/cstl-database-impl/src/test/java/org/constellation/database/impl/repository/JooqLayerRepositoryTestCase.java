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
package org.constellation.database.impl.repository;

import org.constellation.database.api.repository.LayerRepository;
import org.constellation.database.impl.AbstractJooqTestTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JooqLayerRepositoryTestCase extends AbstractJooqTestTestCase {
    
    @Autowired
    private LayerRepository layerRepository;

    @Test
    public void all() {
        dump(layerRepository.findAll());
    }
    
    @Test
    public void getDataSet() {
        layerRepository.getDataSet("zozo");
    }
}
