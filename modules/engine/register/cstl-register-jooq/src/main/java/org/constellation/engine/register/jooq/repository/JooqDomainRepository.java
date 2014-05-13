package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DOMAIN;
import static org.constellation.engine.register.jooq.Tables.SERVICE_X_DOMAIN;
import static org.constellation.engine.register.jooq.Tables.USER_X_DOMAIN_X_DOMAINROLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.jooq.tables.records.DomainRecord;
import org.constellation.engine.register.jooq.tables.records.ServiceXDomainRecord;
import org.constellation.engine.register.jooq.tables.records.UserXDomainXDomainroleRecord;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.jooq.Batch;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JooqDomainRepository extends AbstractJooqRespository<DomainRecord, Domain> implements DomainRepository {

    @Autowired
    private ProviderRepository providerRepository;

    public JooqDomainRepository() {
        super(Domain.class, DOMAIN);
    }

    private RecordMapper<DomainRecord, Domain> mapper = new RecordMapper<DomainRecord, Domain>() {
        @Override
        public Domain map(DomainRecord record) {
            return record.into(Domain.class);
        }
    };

    @Override
    RecordMapper<? super DomainRecord, Domain> getDTOMapper() {
        return mapper;
    }

    @Override
    public Domain findOne(Integer id) {
        Record fetch = dsl.fetchOne(dsl.select().from(DOMAIN).where(DOMAIN.ID.eq(id)));
        if (fetch == null)
            return null;
        return fetch.into(Domain.class);
    }

    @Override
    public Domain save(Domain domain) {
        DomainRecord newRecord = dsl.newRecord(DOMAIN);
        newRecord.setDescription(domain.getDescription());
        newRecord.setName(domain.getName());
        if (newRecord.store() > 0) {
            domain.setId(newRecord.getId());
            return domain;
        }
        return null;
    }

    public int[] addUserToDomain(String userId, int domainId, Set<String> roles) {

        Collection<UserXDomainXDomainroleRecord> records = new ArrayList<UserXDomainXDomainroleRecord>();

        for (String role : roles) {
            UserXDomainXDomainroleRecord newRecord = dsl.newRecord(USER_X_DOMAIN_X_DOMAINROLE);
            newRecord.setDomainId(domainId);
            newRecord.setLogin(userId);
            newRecord.setDomainrole(role);
            records.add(newRecord);
        }

        Batch batchInsert = dsl.batchInsert(records);
        return batchInsert.execute();

    }

    public int removeUserFromDomain(String userId, int domainId) {

        return dsl
                .delete(USER_X_DOMAIN_X_DOMAINROLE)
                .where(USER_X_DOMAIN_X_DOMAINROLE.LOGIN.eq(userId).and(
                        USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId))).execute();

    }

    public void update(Domain domainDTO) {
        dsl.update(DOMAIN).set(DOMAIN.NAME, domainDTO.getName()).set(DOMAIN.DESCRIPTION, domainDTO.getDescription())
                .where(DOMAIN.ID.eq(domainDTO.getId())).execute();
    }

    public int delete(int domainId) {
        return dsl.delete(DOMAIN).where(DOMAIN.ID.eq(domainId)).execute();
    }

    @Override
    public void addServiceToDomain(int serviceId, int domainId) {
        ServiceXDomainRecord newRecord = dsl.newRecord(SERVICE_X_DOMAIN);
        newRecord.setDomainId(domainId);
        newRecord.setServiceId(serviceId);
        newRecord.store();

    }

    @Override
    public int removeServiceFromDomain(int serviceId, int domainId) {
        return dsl.delete(SERVICE_X_DOMAIN)
                .where(SERVICE_X_DOMAIN.SERVICE_ID.eq(serviceId).and(SERVICE_X_DOMAIN.DOMAIN_ID.eq(domainId))).execute();
    }

    @Override
    public int removeUserFromAllDomain(String userId) {
        return dsl.delete(USER_X_DOMAIN_X_DOMAINROLE).where(USER_X_DOMAIN_X_DOMAINROLE.LOGIN.eq(userId)).execute();
    }
}
