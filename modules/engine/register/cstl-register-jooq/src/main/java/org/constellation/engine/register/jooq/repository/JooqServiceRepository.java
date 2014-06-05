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
import static org.constellation.engine.register.jooq.Tables.SERVICE_EXTRA_CONFIG;
import static org.constellation.engine.register.jooq.Tables.USER_X_DOMAIN_X_DOMAINROLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.jooq.Keys;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.User;
import org.constellation.engine.register.jooq.tables.records.ServiceExtraConfigRecord;
import org.constellation.engine.register.jooq.tables.records.ServiceRecord;
import org.constellation.engine.register.repository.ServiceRepository;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.springframework.stereotype.Component;

@Component
public class JooqServiceRepository extends AbstractJooqRespository<ServiceRecord, Service> implements ServiceRepository {

    public JooqServiceRepository() {
        super(Service.class, SERVICE);
    }

    @Override
    public List<Service> findByDataId(int dataId) {
        SelectConditionStep<Record> from = dsl.select().from(SERVICE).join(Tables.LAYER).onKey()
                .where(Tables.LAYER.DATA.eq(dataId));
        return from.fetchInto(Service.class);
    }

    @Override
    public Service findByIdentifierAndType(String identifier, String type) {
        Record one = dsl.select().from(SERVICE)
                .where(SERVICE.IDENTIFIER.eq(identifier).and(SERVICE.TYPE.eq(type.toUpperCase()))).fetchOne();
        if (one == null)
            return null;
        return one.into(Service.class);
    }

    @Override
    public void delete(Integer id) {
        dsl.delete(SERVICE).where(SERVICE.ID.eq(id)).execute();
    }

    @Override
    public Service save(Service service) {

        ServiceRecord newRecord = dsl.newRecord(SERVICE);

        newRecord.setIdentifier(service.getIdentifier());
        newRecord.setOwner(service.getOwner());
        newRecord.setType(service.getType());
        newRecord.setConfig(service.getConfig());

        if (newRecord.store() > 0) {
            return newRecord.into(Service.class);
        }
        return null;
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
        return dsl.select().from(SERVICE_EXTRA_CONFIG).where(SERVICE_EXTRA_CONFIG.ID.eq(id))
                .fetchInto(ServiceExtraConfig.class);
    }

    @Override
    public ServiceExtraConfig getExtraConfig(int id, String filename) {
        return dsl.select().from(SERVICE_EXTRA_CONFIG)
                .where(SERVICE_EXTRA_CONFIG.ID.eq(id).and(SERVICE_EXTRA_CONFIG.FILENAME.eq(filename)))
                .fetchOneInto(ServiceExtraConfig.class);
    }

    @Override
    public Service updateConfig(Service service) {
        dsl.update(SERVICE).set(SERVICE.CONFIG, service.getConfig()).where(SERVICE.ID.eq(service.getId())).execute();
        return null;
    }

    @Override
    public void updateExtraFile(Service service, String fileName, String config) {
        int updateCount = dsl.update(SERVICE_EXTRA_CONFIG).set(SERVICE_EXTRA_CONFIG.CONTENT, config)
                .set(SERVICE_EXTRA_CONFIG.FILENAME, fileName).where(SERVICE_EXTRA_CONFIG.ID.eq(service.getId()))
                .execute();
        if (updateCount == 0) {
            ServiceExtraConfigRecord newRecord = dsl.newRecord(SERVICE_EXTRA_CONFIG);
            newRecord.setContent(config);
            newRecord.setFilename(fileName);
            newRecord.setId(service.getId());
            newRecord.store();
        }

    }

    @Override
    public int updateIsoMetadata(Service service, String metadataId, String metadata) {
        return dsl.update(SERVICE).set(SERVICE.METADATA_ID, metadataId).set(SERVICE.METADATA, metadata)
                .where(SERVICE.ID.eq(service.getId())).execute();
    }

    @Override
    public Map<String, Set<String>> getAccessiblesServicesByType(int domainId, String userName) {

        Result<Record2<String, String>> result = dsl.selectDistinct(SERVICE.IDENTIFIER, SERVICE.TYPE).from(SERVICE)
                .join(Tables.SERVICE_X_DOMAIN).onKey().join(USER_X_DOMAIN_X_DOMAINROLE)
                .on(Tables.SERVICE_X_DOMAIN.DOMAIN_ID.eq(USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID)).join(Tables.USER)
                .on(Tables.USER.ID.eq(USER_X_DOMAIN_X_DOMAINROLE.USER_ID))
                .where(Tables.USER.LOGIN.eq(userName).and(USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId))).fetch();

        Map<String, Set<String>> resultM = new HashMap<>();

        Map<String, Result<Record2<String, String>>> services = result.intoGroups(SERVICE.TYPE);
        for (Entry<String, Result<Record2<String, String>>> serviceEntry : services.entrySet()) {
            resultM.put(serviceEntry.getKey(), new HashSet<String>(serviceEntry.getValue()
                    .getValues(SERVICE.IDENTIFIER)));

        }
        return resultM;
    }

    @Override
    public Service findById(int id) {
        Record one = dsl.select().from(SERVICE).where(SERVICE.ID.eq(id)).fetchOne();
        if (one == null)
            return null;
        return one.into(Service.class);
	}

	@Override
	public int create(Service service) {
		ServiceRecord serviceRecord = dsl.newRecord(SERVICE);
		serviceRecord.setConfig(service.getConfig());
		serviceRecord.setDate(service.getDate());
		serviceRecord.setDescription(service.getDescription());
		serviceRecord.setIdentifier(service.getIdentifier());
		serviceRecord.setMetadata(service.getMetadata());
		serviceRecord.setMetadataId(service.getMetadataId());
		serviceRecord.setType(service.getType());
		serviceRecord.setTitle(service.getTitle());
		serviceRecord.setOwner(service.getOwner());
		serviceRecord.store();
		return serviceRecord.getId().intValue();
	}

}
