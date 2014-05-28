package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.DOMAIN;
import static org.constellation.engine.register.jooq.Tables.DOMAINROLE;
import static org.constellation.engine.register.jooq.Tables.DOMAINROLE_X_PERMISSION;
import static org.constellation.engine.register.jooq.Tables.USER;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.User;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.records.DomainroleRecord;
import org.constellation.engine.register.jooq.tables.records.DomainroleXPermissionRecord;
import org.constellation.engine.register.repository.DomainRoleRepository;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqDomainRoleRepository extends AbstractJooqRespository<DomainroleRecord, DomainRole> implements
        DomainRoleRepository {

    Predicate<Permission> nullPermission = new Predicate<Permission>() {
        @Override
        public boolean match(Permission t) {
            return t.getId() > 0;
        }
    };

    Predicate<Domain> nullDomain = new Predicate<Domain>() {
        @Override
        public boolean match(Domain t) {
            return t.getId() > 0;
        }
    };

    public JooqDomainRoleRepository() {
        super(DomainRole.class, DOMAINROLE);
    }

    @Override
    public List<DomainRole> findAll() {
        List<DomainRole> domainRoles = new ArrayList<DomainRole>();
        Map<Record, Result<Record>> domains = dsl.select().from(DOMAINROLE).leftOuterJoin(DOMAINROLE_X_PERMISSION)
                .onKey().leftOuterJoin(Tables.PERMISSION).onKey().fetch().intoGroups(DOMAINROLE.fields());
        for (Map.Entry<Record, Result<Record>> entry : domains.entrySet()) {
            DomainRole domainRole = entry.getKey().into(DomainRole.class);
            List<Permission> permissions = filter(entry.getValue().into(Permission.class), nullPermission);

            domainRole.setPermissions(permissions);
            domainRoles.add(domainRole);
        }

        return domainRoles;
    }

    @Override
    @Transactional
    public DomainRole save(DomainRole group) {

        DomainRole saved = new DomainRole();
        DomainroleRecord newRecord = dsl.newRecord(DOMAINROLE);
        newRecord.setName(group.getName());
        newRecord.setDescription(group.getDescription());

        newRecord.store();

        saved.setId(newRecord.getId());
        saved.setName(group.getName());
        saved.setDescription(group.getDescription());

        insertPermissions(saved);

        saved.setPermissions(group.getPermissions());

        return saved;
    }

    @Override
    @Transactional
    public DomainRole update(DomainRole domainRole) {

        dsl.update(DOMAINROLE).set(DOMAINROLE.NAME, domainRole.getName())
                .set(DOMAINROLE.DESCRIPTION, domainRole.getDescription()).where(DOMAINROLE.ID.eq(domainRole.getId()))
                .execute();

        dsl.delete(DOMAINROLE_X_PERMISSION).where(DOMAINROLE_X_PERMISSION.DOMAINROLE_ID.eq(domainRole.getId()))
                .execute();

        insertPermissions(domainRole);
        return domainRole;
    }

    private void insertPermissions(DomainRole domainRole) {

        List<UpdatableRecord<DomainroleXPermissionRecord>> records = new ArrayList<UpdatableRecord<DomainroleXPermissionRecord>>();
        for (Permission permission : domainRole.getPermissions()) {
            DomainroleXPermissionRecord permissionRecord = dsl.newRecord(DOMAINROLE_X_PERMISSION);
            permissionRecord.setDomainroleId(domainRole.getId());
            permissionRecord.setPermissionId(permission.getId());
            records.add(permissionRecord);
        }

        dsl.batchInsert(records).execute();
    }

    /**
     * Does not delete system entries.
     */
    @Override
    public int delete(int id) {
        return dsl.delete(DOMAINROLE).where(DOMAINROLE.ID.eq(id).and(DOMAINROLE.SYSTEM.eq(false))).execute();
    }

    @Override
    public DomainRole findOneWithPermission(int id) {
        Result<Record> fetch = dsl.select().from(DOMAINROLE).leftOuterJoin(Tables.DOMAINROLE_X_PERMISSION).onKey()
                .leftOuterJoin(Tables.PERMISSION).onKey().where(DOMAINROLE.ID.eq(id)).fetch();
        Map<Record, Result<Record>> domainRoleEntry = fetch.intoGroups(DOMAINROLE.fields());
        for (Entry<Record, Result<Record>> rec : domainRoleEntry.entrySet()) {
            DomainRole domainRole = rec.getKey().into(DomainRole.class);
            List<Permission> permissions = filter(rec.getValue().into(Permission.class), nullPermission);
            domainRole.setPermissions(permissions);
            return domainRole;
        }
        return null;
    }

    @Override
    public List<Permission> allPermission() {
        return dsl.select().from(Tables.PERMISSION).fetchInto(Permission.class);
    }

    @Override
    public Map<DomainRole, List<Pair<User, List<Domain>>>> findAllWithMembers() {
        Map<DomainRole, List<Pair<User, List<Domain>>>> result = new LinkedHashMap<>();
        Map<Record, Result<Record>> domainRoles = dsl.select().from(DOMAINROLE)
                .leftOuterJoin(Tables.USER_X_DOMAIN_X_DOMAINROLE).onKey().leftOuterJoin(DOMAIN).onKey()
                .leftOuterJoin(USER).onKey().orderBy(DSL.lower(DOMAINROLE.NAME).asc()).fetchGroups(DOMAINROLE.fields());
        for (Entry<Record, Result<Record>> domainRoleEntry : domainRoles.entrySet()) {
            DomainRole domainRole = domainRoleEntry.getKey().into(DomainRole.class);
            Map<Record, Result<Record>> users = domainRoleEntry.getValue().intoGroups(USER.fields());
            List<Pair<User, List<Domain>>> userList = new ArrayList<Pair<User, List<Domain>>>();
            for (Entry<Record, Result<Record>> userRecord : users.entrySet()) {
                User user = userRecord.getKey().into(User.class);
                if (user.getId() == null)
                    continue;
                List<Domain> domains = filter(userRecord.getValue().into(Domain.class), nullDomain);
                userList.add(Pair.of(user, domains));
            }
            result.put(domainRole, userList);
        }
        return result;
    }

}
