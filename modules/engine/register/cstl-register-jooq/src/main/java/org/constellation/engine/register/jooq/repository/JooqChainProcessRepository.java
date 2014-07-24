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
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.helper.ChainProcessHelper;
import static org.constellation.engine.register.jooq.Tables.CHAIN_PROCESS;
import org.constellation.engine.register.jooq.tables.records.ChainProcessRecord;
import org.constellation.engine.register.repository.ChainProcessRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class JooqChainProcessRepository extends AbstractJooqRespository<ChainProcessRecord, ChainProcess> implements ChainProcessRepository {
    
    public JooqChainProcessRepository() {
        super(ChainProcess.class, CHAIN_PROCESS);
    }
    
    public List<ChainProcess> findAll() {
        return dsl.select().from(CHAIN_PROCESS).fetchInto(ChainProcess.class);
    }

    @Override
    public ChainProcess create(ChainProcess chain) {
        ChainProcessRecord newRecord = ChainProcessHelper.copy(chain, dsl.newRecord(CHAIN_PROCESS));
        newRecord.store();
        return newRecord.into(ChainProcess.class);
    }
}
