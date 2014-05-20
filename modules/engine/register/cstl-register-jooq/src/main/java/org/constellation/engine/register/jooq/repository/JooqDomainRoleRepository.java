package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DOMAINROLE;
import static org.constellation.engine.register.jooq.Tables.DOMAINROLE_X_PERMISSION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.DomainroleRecord;
import org.constellation.engine.register.jooq.tables.records.DomainroleXPermissionRecord;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdatableRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqDomainRoleRepository extends AbstractJooqRespository<DomainroleRecord, DomainRole> implements
        DomainRoleRepository {

    public JooqDomainRoleRepository() {
        super(DomainRole.class, DOMAINROLE);
    }

    @Override
    public List<DomainRole> findAll() {
        List<DomainRole> domainRoles = new ArrayList<DomainRole>();
        Map<Record, Result<Record>> domains = dsl.select().from(DOMAINROLE).leftOuterJoin(DOMAINROLE_X_PERMISSION).onKey()
                .fetch().intoGroups(DOMAINROLE.fields());
        for (Map.Entry<Record, Result<Record>> entry : domains.entrySet()) {
            DomainRole domainRole = entry.getKey().into(DomainRole.class);
            List<String> permissions = entry.getValue().getValues(DOMAINROLE_X_PERMISSION.PERMISSION_ID);
            domainRole.setPermissions(new HashSet<String>(permissions));
            domainRoles.add(domainRole);
        }

        return domainRoles;
    }

    @Override
    @Transactional
    public DomainRole save(DomainRole group) {
        DomainroleRecord newRecord = dsl.newRecord(DOMAINROLE);
        newRecord.setName(group.getName());
        newRecord.setDescription(group.getDescription());
        newRecord.store();
        group.setName(group.getName());
        
        insertPermissions(group);
        
        DomainRole updated = new DomainRole();
        updated.setName(group.getName());
        updated.setDescription(group.getDescription());
        updated.setPermissions(new HashSet<String>(group.getPermissions()));
        
        return updated;
    }

    @Override
    @Transactional
    public DomainRole update(DomainRole domainRole) {

        dsl.update(DOMAINROLE).set(DOMAINROLE.DESCRIPTION, domainRole.getDescription())
                .where(DOMAINROLE.NAME.eq(domainRole.getName())).execute();

        dsl.delete(DOMAINROLE_X_PERMISSION).where(DOMAINROLE_X_PERMISSION.DOMAINROLE_ID.eq(domainRole.getName()))
                .execute();

        insertPermissions(domainRole);
        return domainRole;
    }

    private void insertPermissions(DomainRole domainRole) {
        List<UpdatableRecord<DomainroleXPermissionRecord>> records = new ArrayList<UpdatableRecord<DomainroleXPermissionRecord>>();
        for (String permission : domainRole.getPermissions()) {
            DomainroleXPermissionRecord permissionRecord = dsl.newRecord(DOMAINROLE_X_PERMISSION);
            permissionRecord.setDomainroleId(domainRole.getName());
            permissionRecord.setPermissionId(permission);
            records.add(permissionRecord);
        }

        dsl.batchInsert(records).execute();
    }

    @Override
    public void delete(String name) {
        dsl.delete(DOMAINROLE).where(DOMAINROLE.NAME.eq(name)).execute();
    }

    @Override
    public DomainRole findOneWithPermission(String name) {
        Result<Record> fetch = dsl.select().from(DOMAINROLE).leftOuterJoin(Tables.DOMAINROLE_X_PERMISSION).onKey()
                .fetch();
        Map<Record, Result<Record>> intoGroups = fetch.intoGroups(DOMAINROLE.fields());
        for (Record rec : intoGroups.keySet()) {
            DomainRole domainRole = rec.into(DomainRole.class);
            for (Result<Record> result : intoGroups.values()) {

            }
            return domainRole;
        }
        return null;
    }

}
