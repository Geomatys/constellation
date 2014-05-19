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

import static org.constellation.engine.register.jooq.Tables.SERVICE;

import java.util.List;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.ServiceRecord;
import org.constellation.engine.register.repository.ServiceRepository;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Component;

@Component
public class JooqServiceRepository extends AbstractJooqRespository<ServiceRecord, Service> implements ServiceRepository {

    public JooqServiceRepository() {
        super(Service.class, SERVICE);
    }

    @Override
    public List<Service> findByDataId(int dataId) {
        SelectConditionStep<Record> from = dsl.select().from(SERVICE).join(Tables.LAYER).onKey().where(Tables.LAYER.DATA.eq(dataId));
        return from.fetchInto(Service.class);
    }

    @Override
    public Service findByIdentifierAndType(String identifier, String type) {
        Record one = dsl.select().from(SERVICE).where(SERVICE.IDENTIFIER.eq(identifier).and(SERVICE.TYPE.eq(type))).fetchOne();
        if(one == null)
            return null;
        return one.into(Service.class);
    }

    @Override
    public void delete(Integer id) {
        dsl.delete(SERVICE).where(SERVICE.ID.eq(id)).execute();
    }

    @Override
    public List<String> findIdentifiersByType(String type) {
         return dsl.select(SERVICE.IDENTIFIER).from(SERVICE).where(SERVICE.TYPE.eq(type)).fetch(SERVICE.IDENTIFIER);
    }

    @Override
    public ServiceMetaData findMetaDataForLangByIdentifierAndType(String identifier, String serviceType, String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ServiceExtraConfig> getExtraConfig(int id) {
        // return
        // dsl.select().from(SERVICE_EXTRA_CONFIG).where(SERVICE_EXTRA_CONFIG.ID.eq(id)).fetchInto(ServiceE)
        //
        // }

        return null;
    }

}
