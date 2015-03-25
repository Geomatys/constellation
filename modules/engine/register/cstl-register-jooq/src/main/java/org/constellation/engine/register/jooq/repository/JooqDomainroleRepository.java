package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.CSTL_USER;
import static org.constellation.engine.register.jooq.Tables.DOMAIN;
import static org.constellation.engine.register.jooq.Tables.DOMAINROLE;
import static org.constellation.engine.register.jooq.Tables.DOMAINROLE_X_PERMISSION;
import static org.constellation.engine.register.jooq.Tables.USER_X_DOMAIN_X_DOMAINROLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;
import org.constellation.engine.register.jooq.tables.pojos.Permission;
import org.constellation.engine.register.jooq.tables.records.DomainroleRecord;
import org.constellation.engine.register.jooq.tables.records.DomainroleXPermissionRecord;
import org.constellation.engine.register.repository.DomainroleRepository;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

@Component
public class JooqDomainroleRepository extends AbstractJooqRespository<DomainroleRecord, Domainrole> implements
        DomainroleRepository {

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

    public JooqDomainroleRepository() {
        super(Domainrole.class, DOMAINROLE);
    }

    @Override
    public Map<Domainrole, List<Permission>> findAllWithPermission() {
        Map<Domainrole, List<Permission>> result = new HashMap<>();
        Map<Record, Result<Record>> domains = dsl.select().from(DOMAINROLE).leftOuterJoin(DOMAINROLE_X_PERMISSION)
                .onKey().leftOuterJoin(Tables.PERMISSION).onKey().fetch().intoGroups(DOMAINROLE.fields());
        for (Map.Entry<Record, Result<Record>> entry : domains.entrySet()) {
            Domainrole domainRole = entry.getKey().into(Domainrole.class);
            List<Permission> permissions = filter(entry.getValue().into(Permission.class), nullPermission);

            result.put(domainRole, permissions);
        }

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Domainrole createWithPermissions(Domainrole group, List<Permission> permissions) {

        Domainrole saved = new Domainrole();
        DomainroleRecord newRecord = dsl.newRecord(DOMAINROLE);
        newRecord.setName(group.getName());
        newRecord.setDescription(group.getDescription());

        newRecord.store();

        saved.setId(newRecord.getId());
        saved.setName(group.getName());
        saved.setDescription(group.getDescription());

        insertPermissions(saved, permissions);

        return saved;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Domainrole updateWithPermissions(Domainrole domainRole, List<Permission> permissions) {

        dsl.update(DOMAINROLE).set(DOMAINROLE.NAME, domainRole.getName())
                .set(DOMAINROLE.DESCRIPTION, domainRole.getDescription()).where(DOMAINROLE.ID.eq(domainRole.getId()))
                .execute();

        dsl.delete(DOMAINROLE_X_PERMISSION).where(DOMAINROLE_X_PERMISSION.DOMAINROLE_ID.eq(domainRole.getId()))
                .execute();

        insertPermissions(domainRole, permissions);
        return domainRole;
    }

    private void insertPermissions(Domainrole domainRole, List<Permission> permissions) {

        List<UpdatableRecord<DomainroleXPermissionRecord>> records = new ArrayList<UpdatableRecord<DomainroleXPermissionRecord>>();
        for (Permission permission : permissions) {
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
    @Transactional(propagation = Propagation.MANDATORY)
    public int delete(int id) {
        return dsl.delete(DOMAINROLE).where(DOMAINROLE.ID.eq(id).and(DOMAINROLE.SYSTEM.eq(false))).execute();
    }

    @Override
    public Optional<Pair<Domainrole, List<Permission>>> findOneWithPermission(int id) {
        Result<Record> fetch = dsl.select().from(DOMAINROLE).leftOuterJoin(Tables.DOMAINROLE_X_PERMISSION).onKey()
                .leftOuterJoin(Tables.PERMISSION).onKey().where(DOMAINROLE.ID.eq(id)).fetch();
        Map<Record, Result<Record>> domainRoleEntry = fetch.intoGroups(DOMAINROLE.fields());
        for (Entry<Record, Result<Record>> rec : domainRoleEntry.entrySet()) {
            Domainrole domainRole = rec.getKey().into(Domainrole.class);
            List<Permission> permissions = filter(rec.getValue().into(Permission.class), nullPermission);
            return Optional.of(Pair.of(domainRole, permissions));
        }
        return Optional.absent();
    }

    @Override
    public List<Permission> allPermission() {
        return dsl.select().from(Tables.PERMISSION).fetchInto(Permission.class);
    }

    @Override
    public Map<Domainrole, List<Pair<CstlUser, List<Domain>>>> findAllWithMembers() {
        Map<Domainrole, List<Pair<CstlUser, List<Domain>>>> result = new LinkedHashMap<>();
        Map<Record, Result<Record>> domainRoles = dsl.select().from(DOMAINROLE)
                .leftOuterJoin(Tables.USER_X_DOMAIN_X_DOMAINROLE).onKey().leftOuterJoin(DOMAIN).onKey()
                .leftOuterJoin(CSTL_USER).onKey().orderBy(DSL.lower(DOMAINROLE.NAME).asc()).fetchGroups(DOMAINROLE.fields());
        for (Entry<Record, Result<Record>> domainRoleEntry : domainRoles.entrySet()) {
            Domainrole domainRole = domainRoleEntry.getKey().into(Domainrole.class);
            Map<Record, Result<Record>> users = domainRoleEntry.getValue().intoGroups(CSTL_USER.fields());
            List<Pair<CstlUser, List<Domain>>> userList = new ArrayList<Pair<CstlUser, List<Domain>>>();
            for (Entry<Record, Result<Record>> userRecord : users.entrySet()) {
                CstlUser user = userRecord.getKey().into(CstlUser.class);
                if (user.getId() == null)
                    continue;
                List<Domain> domains = filter(userRecord.getValue().into(Domain.class), nullDomain);
                userList.add(Pair.of(user, domains));
            }
            result.put(domainRole, userList);
        }
        return result;
    }

    @Override
    public Map<Domainrole, List<Integer>> findAllWithPermissions(int... serviceWriteAccessPermissionId) {
        Map<Domainrole, List<Integer>> result = new HashMap<Domainrole, List<Integer>>();
        Result<Record> fetch = dsl.select().from(DOMAINROLE).join(DOMAINROLE_X_PERMISSION).onKey()
                .where(DOMAINROLE_X_PERMISSION.PERMISSION_ID.in(Ints.asList(serviceWriteAccessPermissionId))).fetch();
        for (Entry<Record, Result<Record>> e : fetch.intoGroups(DOMAINROLE.fields()).entrySet()) {
            Domainrole domainRole = e.getKey().into(Domainrole.class);
            List<Integer> values = e.getValue().getValues(DOMAINROLE_X_PERMISSION.PERMISSION_ID);
            result.put(domainRole, values);
        }
        return result;
    }

    @Override
    public List<Domainrole> findUserDomainroles(int id, int domainId) {
        return findBy(DOMAINROLE.ID.in(dsl
                .select(USER_X_DOMAIN_X_DOMAINROLE.DOMAINROLE_ID)
                .from(USER_X_DOMAIN_X_DOMAINROLE)
                .where(USER_X_DOMAIN_X_DOMAINROLE.USER_ID.eq(id).and(
                        USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId)))));
    }
}
