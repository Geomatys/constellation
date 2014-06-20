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
import static org.constellation.engine.register.jooq.Tables.DATA_I18N;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.DataI18n;
import org.constellation.engine.register.helper.DataHelper;
import org.constellation.engine.register.i18n.DataWithI18N;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.DataRecord;
import org.constellation.engine.register.repository.DataRepository;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.List;

@Component
public class JooqDataRepository extends AbstractJooqRespository<DataRecord, Data> implements DataRepository {

    public JooqDataRepository() {
        super(Data.class, DATA);
    }

    @Override
    public Data findByNameAndNamespaceAndProviderIdentifier(String name, String namespace, String providerIdentifier) {
        return dsl.select().from(DATA).where(DATA.VISIBLE.eq(true))
                .and(DATA.NAMESPACE.eq(namespace)).and(DATA.NAME.eq(name))
                .and(DATA.PROVIDER.eq(dsl.select(PROVIDER.ID).from(PROVIDER).where(PROVIDER.IDENTIFIER.eq(providerIdentifier))))
                .fetchOneInto(Data.class);
    }
    
    @Override
    public Data findById(int id) {
        return dsl.select().from(DATA).where(DATA.ID.eq(id)).fetchOneInto(Data.class);
    }
    
    @Override
    public Data fromLayer(String layerAlias, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Data create(Data data) {
        DataRecord newRecord = dsl.newRecord(DATA);
        newRecord.setDate(data.getDate());
        newRecord.setIsoMetadata(data.getIsoMetadata());
        newRecord.setMetadata(data.getMetadata());
        newRecord.setName(data.getName());
        newRecord.setNamespace(data.getNamespace());
        newRecord.setOwner(data.getOwner());
        newRecord.setProvider(data.getProvider());
        newRecord.setType(data.getType());
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
        if (namespaceURI != null)
            return whereClause.and(DATA.NAMESPACE.eq(namespaceURI));
        return whereClause;
    }

    @Override
    public Data findDataFromProvider(String namespaceURI, String localPart, String providerId) {
        return dsl.select().from(DATA).join(Tables.PROVIDER).onKey().where(Tables.PROVIDER.IDENTIFIER.eq(providerId))
                .fetchOneInto(Data.class);
    }

    @Override
    public Data findByMetadataId(String metadataId) {
        return dsl.select().from(DATA).where(DATA.METADATA_ID.eq(metadataId)).fetchOneInto(Data.class);
    }

    @Override
    public List<Data> findByProviderId(Integer id) {
        return dsl.select().from(DATA).where(DATA.PROVIDER.eq(id)).fetchInto(Data.class);
    }


    @Override
    public DataWithI18N getDescription(Data data) {
        Result<Record> fetch = dsl.select().from(DATA_I18N).where(DATA_I18N.DATA_ID.eq(data.getId())).fetch();
        ImmutableMap<String, DataI18n> dataI18ns = Maps.uniqueIndex(fetch.into(DataI18n.class), new Function<DataI18n, String>() {
            @Override
            public String apply(DataI18n input) {
                return input.getLang();
            }
        });
        return new DataWithI18N(data, dataI18ns);
    }


    @Override
    public Data findByNameAndNamespaceAndProviderId(String localPart, String namespaceURI, Integer providerId) {
        return dsl.select().from(DATA).where(DATA.PROVIDER.eq(providerId))
                .and(DATA.NAME.eq(localPart)).and(DATA.NAMESPACE.eq(namespaceURI))
                .fetchOneInto(Data.class);
    }

	@Override
    public void update(Data data) {
		
		dsl.update(DATA).set(DATA.DATE,data.getDate())
		.set(DATA.ISO_METADATA,data.getIsoMetadata())
		.set(DATA.METADATA, data.getMetadata())
		.set(DATA.METADATA_ID, data.getMetadataId())
		.set(DATA.NAME, data.getName())
		.set(DATA.NAMESPACE, data.getNamespace())
		.set(DATA.OWNER, data.getOwner())
		.set(DATA.PROVIDER, data.getProvider())
		.set(DATA.SENSORABLE, data.isSensorable())
		.set(DATA.SUBTYPE, data.getSubtype())
		.set(DATA.TYPE, data.getType())
		.set(DATA.VISIBLE, data.isVisible())
		.execute();
	    
    }


}
