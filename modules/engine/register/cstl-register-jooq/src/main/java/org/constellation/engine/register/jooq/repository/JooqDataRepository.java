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

import static org.constellation.engine.register.jooq.Tables.DATA;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.jooq.tables.records.DataRecord;
import org.constellation.engine.register.repository.DataRepository;
import org.jooq.Condition;
import org.springframework.stereotype.Component;

@Component
public class JooqDataRepository extends AbstractJooqRespository<DataRecord, Data> implements DataRepository {

    public JooqDataRepository() {
        super(Data.class, DATA);
    }

    @Override
    public Data findByNameAndNamespaceAndProviderId(String name, String namespace, String providerIdentifier) {
        dsl.insertInto(DATA).select(dsl.select().from(DATA).where(DATA.VISIBLE.eq(true))).execute();
        return null;
    }

    @Override
    public Data fromLayer(String layerAlias, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Data save(Data data) {
        DataRecord newRecord = dsl.newRecord(DATA);
        newRecord.setDate(data.getDate());
        newRecord.setDescription(data.getDescription());
        newRecord.setIsoMetadata(data.getIsoMetadata());
        newRecord.setMetadata(data.getMetadata());
        newRecord.setName(data.getName());
        newRecord.setNamespace(data.getNamespace());
        newRecord.setOwner(data.getOwner());
        newRecord.setProvider(data.getProviderId());
        newRecord.setType(data.getType());
        newRecord.setTitle(data.getTitle());
        newRecord.store();
        return newRecord.into(Data.class);
    }

    @Override
    public int delete(int id) {
        return dsl.delete(DATA).where(DATA.ID.eq(id)).execute();
    }

    @Override
    public int delete(String namespaceURI, String localPart, int providerId) {
        Condition whereClause = buildDeleteWhereClause(namespaceURI, localPart, providerId);
        return dsl.delete(DATA).where(whereClause).execute();
        
    }

    private Condition buildDeleteWhereClause(String namespaceURI, String localPart, int providerId) {
        Condition whereClause = DATA.NAME.eq(localPart).and(DATA.PROVIDER.eq(providerId));
        if(namespaceURI!=null)
            return whereClause.and(DATA.NAMESPACE.eq(namespaceURI));
        return whereClause;
    }

}
