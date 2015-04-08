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
import static org.constellation.engine.register.jooq.Tables.LAYER;
import static org.constellation.engine.register.jooq.Tables.METADATA;
import static org.constellation.engine.register.jooq.Tables.METADATA_X_CSW;
import static org.constellation.engine.register.jooq.Tables.SERVICE;
import static org.constellation.engine.register.jooq.Tables.SERVICE_DETAILS;
import static org.constellation.engine.register.jooq.Tables.SERVICE_EXTRA_CONFIG;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Metadata;
import org.constellation.engine.register.jooq.tables.pojos.Service;
import org.constellation.engine.register.jooq.tables.pojos.ServiceDetails;
import org.constellation.engine.register.jooq.tables.pojos.ServiceExtraConfig;
import org.constellation.engine.register.jooq.tables.records.ServiceDetailsRecord;
import org.constellation.engine.register.jooq.tables.records.ServiceExtraConfigRecord;
import org.constellation.engine.register.jooq.tables.records.ServiceRecord;
import org.constellation.engine.register.repository.ServiceRepository;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqServiceRepository extends AbstractJooqRespository<ServiceRecord, Service> implements ServiceRepository {


    public JooqServiceRepository() {
        super(Service.class, SERVICE);
    }

    @Override
    public List<Service> findByDataId(int dataId) {
            SelectConditionStep<Record> from = dsl.select().from(SERVICE).join(Tables.LAYER).onKey()
                .where(Tables.LAYER.DATA.eq(dataId));
        final List<Service> layerServices = from.fetchInto(Service.class);
        
        final List<Service> cswServices = dsl.select(SERVICE.fields()).from(Arrays.asList(SERVICE,METADATA_X_CSW,METADATA))
                .where(METADATA_X_CSW.CSW_ID.eq(SERVICE.ID))
                .and(METADATA_X_CSW.METADATA_ID.eq(METADATA.ID))
                .and(METADATA.DATA_ID.eq(dataId)).fetchInto(Service.class);
        
        layerServices.addAll(cswServices);
        return layerServices;
    }

    @Override
    public Service findByIdentifierAndType(String identifier, String type) {
        return  dsl.select().from(SERVICE)
                .where(SERVICE.IDENTIFIER.eq(identifier).and(SERVICE.TYPE.equalIgnoreCase(type))).fetchOneInto(Service.class);
       
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(Integer id) {
        dsl.delete(SERVICE_DETAILS).where(SERVICE_DETAILS.ID.eq(id)).execute();
        dsl.delete(SERVICE_EXTRA_CONFIG).where(SERVICE_EXTRA_CONFIG.ID.eq(id)).execute();
        dsl.delete(SERVICE).where(SERVICE.ID.eq(id)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Service update(Service service) {
        dsl.update(SERVICE)
                .set(SERVICE.DATE, service.getDate())
                .set(SERVICE.CONFIG, service.getConfig())
                .set(SERVICE.IDENTIFIER, service.getIdentifier())
                .set(SERVICE.OWNER, service.getOwner())
                .set(SERVICE.STATUS, service.getStatus())
                .set(SERVICE.TYPE, service.getType())
                .set(SERVICE.VERSIONS, service.getVersions())
                .where(SERVICE.ID.eq(service.getId())).execute();
        return service;
    }

    @Override
    public List<String> findIdentifiersByType(String type) {
        return dsl.select(SERVICE.IDENTIFIER).from(SERVICE).where(SERVICE.TYPE.eq(type)).fetch(SERVICE.IDENTIFIER);
    }
    
    @Override
    public List<Service> findByType(String type) {
        SelectConditionStep<Record> from = dsl.select().from(SERVICE).where(SERVICE.TYPE.eq(type.toLowerCase()));
        return from.fetchInto(Service.class);
    }

    @Override
    public ServiceDetails getServiceDetailsForDefaultLang(int serviceId) {
        return dsl.select().from(SERVICE_DETAILS).where(SERVICE_DETAILS.ID.eq(serviceId)).and(SERVICE_DETAILS.DEFAULT_LANG.eq(true)).fetchOneInto(ServiceDetails.class);
    }

    @Override
    public ServiceDetails getServiceDetails(int serviceId, String language) {
        if (language != null) {
            return dsl.select().from(SERVICE_DETAILS).where(SERVICE_DETAILS.ID.eq(serviceId)).and(SERVICE_DETAILS.LANG.eq(language)).fetchOneInto(ServiceDetails.class);
        } else {
            return dsl.select().from(SERVICE_DETAILS).where(SERVICE_DETAILS.ID.eq(serviceId)).and(SERVICE_DETAILS.DEFAULT_LANG.eq(true)).fetchOneInto(ServiceDetails.class);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void createOrUpdateServiceDetails(ServiceDetails serviceDetails) {
        final ServiceDetails old = getServiceDetails(serviceDetails.getId(), serviceDetails.getLang());
        if (old!=null){
            dsl.update(SERVICE_DETAILS).set(SERVICE_DETAILS.CONTENT, serviceDetails.getContent())
                    .set(SERVICE_DETAILS.DEFAULT_LANG, serviceDetails.getDefaultLang())
                    .where(SERVICE_DETAILS.ID.eq(serviceDetails.getId()))
                    .and(SERVICE_DETAILS.LANG.eq(serviceDetails.getLang()))

                    .execute();
        } else {
            ServiceDetailsRecord newRecord = dsl.newRecord(SERVICE_DETAILS);
            newRecord.setContent(serviceDetails.getContent());
            newRecord.setLang(serviceDetails.getLang());
            newRecord.setId(serviceDetails.getId());
            newRecord.setDefaultLang(serviceDetails.getDefaultLang());
            newRecord.store();
        }
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
    @Transactional(propagation = Propagation.MANDATORY)
    public Service updateConfig(Service service) {
        dsl.update(SERVICE).set(SERVICE.CONFIG, service.getConfig()).where(SERVICE.ID.eq(service.getId())).execute();
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
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
    public Map<String, Set<String>> getAccessiblesServicesByType(String userName) {

        Result<Record2<String, String>> result = dsl.selectDistinct(SERVICE.IDENTIFIER, SERVICE.TYPE).from(SERVICE)
                .fetch();

        Map<String, Set<String>> resultM = new HashMap<>();

        Map<String, Result<Record2<String, String>>> services = result.intoGroups(SERVICE.TYPE);
        for (Entry<String, Result<Record2<String, String>>> serviceEntry : services.entrySet()) {
            resultM.put(serviceEntry.getKey(), new HashSet<>(serviceEntry.getValue()
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
    @Transactional(propagation = Propagation.MANDATORY)
    public int create(Service service) {
        ServiceRecord serviceRecord = dsl.newRecord(SERVICE);
        serviceRecord.setConfig(service.getConfig());
        serviceRecord.setDate(service.getDate());
        serviceRecord.setIdentifier(service.getIdentifier());
        serviceRecord.setType(service.getType());
        serviceRecord.setOwner(service.getOwner());
        serviceRecord.setStatus(service.getStatus());
        serviceRecord.setVersions(service.getVersions());
        serviceRecord.store();
        return serviceRecord.getId();
    }


    @Override
    public Service findByMetadataId(String metadataId) {
        return dsl.select().from(SERVICE).join(METADATA).onKey(METADATA.SERVICE_ID).where(METADATA.METADATA_ID.eq(metadataId)).fetchOneInto(Service.class);
    }

    @Override
    public List<Data> findDataByServiceId(Integer id) {
        return dsl.select().from(DATA).join(LAYER).on(LAYER.DATA.eq(DATA.ID)).join(SERVICE).on(LAYER.SERVICE.eq(SERVICE.ID)).where(SERVICE.ID.eq(id))
        .fetchInto(Data.class);
    }



    @Override
    public Metadata getMetadata(Integer id) {
        return dsl.select().from(METADATA).where(METADATA.SERVICE_ID.eq(id)).fetchOneInto(Metadata.class);
    }

}
