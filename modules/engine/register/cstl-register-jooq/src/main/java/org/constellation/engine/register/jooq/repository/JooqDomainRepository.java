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
import static org.constellation.engine.register.jooq.Tables.DATA_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.DOMAIN;
import static org.constellation.engine.register.jooq.Tables.PROVIDER;
import static org.constellation.engine.register.jooq.Tables.SERVICE_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.USER_X_DOMAIN_X_DOMAINROLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.records.DataXDomainRecord;
import org.constellation.engine.register.jooq.tables.records.DomainRecord;
import org.constellation.engine.register.jooq.tables.records.ServiceXDomainRecord;
import org.constellation.engine.register.jooq.tables.records.UserXDomainXDomainroleRecord;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.jooq.Batch;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqDomainRepository extends AbstractJooqRespository<DomainRecord, Domain> implements DomainRepository {

    @Autowired
    private ProviderRepository providerRepository;

    public JooqDomainRepository() {
        super(Domain.class, DOMAIN);
    }

    @Override
    public List<Domain> findAll() {
        return dsl.select().from(Tables.DOMAIN).fetchInto(Domain.class);
    }

    @Override
    public List<Domain> findByIds(List<Integer> fetch) {
        if(fetch.isEmpty())
            return Collections.emptyList();
        return findBy(DOMAIN.ID.in(fetch));
    }

    @Override
    public List<Domain> findByIdsNotIn(List<Integer> fetch) {
        if (fetch.isEmpty())
            return findBy(null);
        return findBy(DOMAIN.ID.notIn(fetch));
    }

    @Override
    public List<Domain> findAllByUserId(int userId) {
        return dsl.select().from(Tables.DOMAIN).join(Tables.USER_X_DOMAIN_X_DOMAINROLE).onKey()
                .where(Tables.USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(userId).and(DOMAIN.SYSTEM.eq(false)))
                .fetchInto(Domain.class);
    }

    @Override
    public Domain findOne(Integer id) {
        Record fetch = dsl.fetchOne(dsl.select().from(DOMAIN).where(DOMAIN.ID.eq(id)));
        if (fetch == null)
            return null;
        return fetch.into(Domain.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Domain save(Domain domain) {
        DomainRecord newRecord = dsl.newRecord(DOMAIN);
        newRecord.setDescription(domain.getDescription());
        newRecord.setName(domain.getName());
        if (newRecord.store() > 0) {
            Domain result = new Domain();
            result.setDescription(domain.getDescription());
            result.setName(domain.getName());
            result.setId(newRecord.getId());
            return result;
        }
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int[] addUserToDomain(int userId, int domainId, Set<Integer> roles) {

        Collection<UserXDomainXDomainroleRecord> records = new ArrayList<UserXDomainXDomainroleRecord>();

        for (Integer role : roles) {
            UserXDomainXDomainroleRecord newRecord = dsl.newRecord(USER_X_DOMAIN_X_DOMAINROLE);
            newRecord.setDomainId(domainId);
            newRecord.setUserId(userId);
            newRecord.setDomainroleId(role);
            records.add(newRecord);
        }

        Batch batchInsert = dsl.batchInsert(records);
        return batchInsert.execute();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int removeUserFromDomain(int userId, int domainId) {

        return dsl
                .delete(USER_X_DOMAIN_X_DOMAINROLE)
                .where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(userId).and(
                        USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId))).execute();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Domain update(Domain domainDTO) {
        dsl.update(DOMAIN).set(DOMAIN.NAME, domainDTO.getName()).set(DOMAIN.DESCRIPTION, domainDTO.getDescription())
                .where(DOMAIN.ID.eq(domainDTO.getId())).execute();
        return new Domain(domainDTO.getId(), domainDTO.getName(), domainDTO.getDescription(), domainDTO.getSystem());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(int domainId) {
        return dsl.delete(DOMAIN).where(DOMAIN.ID.eq(domainId)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void addServiceToDomain(int serviceId, int domainId) {
        ServiceXDomainRecord newRecord = dsl.newRecord(SERVICE_X_DOMAIN);
        newRecord.setDomainId(domainId);
        newRecord.setServiceId(serviceId);
        newRecord.store();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int removeServiceFromDomain(int serviceId, int domainId) {
        return dsl.delete(SERVICE_X_DOMAIN)
                .where(SERVICE_X_DOMAIN.SERVICE_ID.eq(serviceId).and(SERVICE_X_DOMAIN.DOMAIN_ID.eq(domainId)))
                .execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int removeUserFromAllDomain(int userId) {
        return dsl.delete(USER_X_DOMAIN_X_DOMAINROLE).where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(userId)).execute();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int addDataToDomain(int dataId, int domainId) {
        DataXDomainRecord record = dsl.newRecord(Tables.DATA_X_DOMAIN);
        record.setDataId(dataId);
        record.setDomainId(domainId);
        return record.store();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int removeDataFromDomain(int dataId, int domainId) {
        return dsl.delete(DATA_X_DOMAIN)
                .where(DATA_X_DOMAIN.DATA_ID.eq(dataId).and(DATA_X_DOMAIN.DOMAIN_ID.eq(domainId))).execute();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int removeAllDataFromDomain(int domainId) {
        return dsl.delete(DATA_X_DOMAIN).where(DATA_X_DOMAIN.DOMAIN_ID.eq(domainId)).execute();

    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public int addProviderDataToDomain(String identifier, int activeDomainId) {
        org.jooq.Field<Integer> field = DSL.val(activeDomainId);
        return dsl
                .insertInto(DATA_X_DOMAIN)
                .select(dsl
                        .select(DATA.ID, field)
                        .from(DATA)
                        .join(PROVIDER)
                        .onKey()
                        .where(PROVIDER.IDENTIFIER.eq(identifier))
                        .and(DATA.ID.notIn(dsl.select(DATA_X_DOMAIN.DATA_ID).from(DATA_X_DOMAIN)
                                .where(DATA_X_DOMAIN.DOMAIN_ID.eq(activeDomainId))))).execute();
    }

    @Override
    public Domain findDefaultByUserId(Integer id) {
        return dsl.select().from(DOMAIN).join(USER_X_DOMAIN_X_DOMAINROLE).onKey()
                .where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(id)).orderBy(DOMAIN.ID).limit(1)
                .fetchOneInto(Domain.class);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Set<Integer> updateUserInDomain(int userId, int domainId, Set<Integer> roles) {
        dsl.delete(USER_X_DOMAIN_X_DOMAINROLE)
                .where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(userId).and(
                        USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId))).execute();
        addUserToDomain(userId, domainId, roles);
        return roles;
    }

    @Override
    public Set<Integer> findUserDomainIdsWithPermission(int userId, int permissionId) {
        List<Integer> fetch = dsl
                .select(Tables.DOMAIN.ID)
                .from(DOMAIN)
                .join(USER_X_DOMAIN_X_DOMAINROLE)
                .onKey()
                .join(Tables.DOMAINROLE_X_PERMISSION)
                .on(USER_X_DOMAIN_X_DOMAINROLE.DOMAINROLE_ID.eq(Tables.DOMAINROLE_X_PERMISSION.DOMAINROLE_ID))
                .where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(userId).and(
                        Tables.DOMAINROLE_X_PERMISSION.PERMISSION_ID.eq(permissionId))).fetch(DOMAIN.ID);
        return new HashSet<>(fetch);
    }

    @Override
    public List<Domain> findByLinkedService(int serviceId) {
                
        return findBy(DOMAIN.ID.in(dsl.select(SERVICE_X_DOMAIN.DOMAIN_ID).from(SERVICE_X_DOMAIN)
                .where(SERVICE_X_DOMAIN.SERVICE_ID.eq(serviceId))));
    }

    @Override
    public List<Domain> findByLinkedData(int dataId) {
        return findBy(DOMAIN.ID.in(dsl.select(DATA_X_DOMAIN.DOMAIN_ID).from(DATA_X_DOMAIN)
                .where(DATA_X_DOMAIN.DATA_ID.eq(dataId))));
    }

}
