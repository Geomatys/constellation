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

import com.google.common.base.Optional;

import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainUser;
import org.constellation.engine.register.Domainrole;
import org.constellation.engine.register.helper.CstlUserHelper;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.UserXDomainXDomainrole;
import org.constellation.engine.register.jooq.tables.UserXRole;
import org.constellation.engine.register.jooq.tables.records.CstlUserRecord;
import org.constellation.engine.register.jooq.tables.records.UserXRoleRecord;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.jooq.Condition;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.UpdateConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.constellation.engine.register.jooq.Tables.CSTL_USER;
import static org.constellation.engine.register.jooq.Tables.USER_X_DOMAIN_X_DOMAINROLE;
import static org.constellation.engine.register.jooq.Tables.USER_X_ROLE;

@Component
public class JooqUserRepository extends AbstractJooqRespository<CstlUserRecord, CstlUser> implements UserRepository {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private DomainRepository domainRepository;

    private org.constellation.engine.register.jooq.tables.CstlUser userTable = CSTL_USER.as("u");
    private UserXRole userXroleTable = Tables.USER_X_ROLE.as("uXr");
    private org.constellation.engine.register.jooq.tables.Domain domainTable = Tables.DOMAIN.as("d");

    private UserXDomainXDomainrole UDD = Tables.USER_X_DOMAIN_X_DOMAINROLE.as("uxdr");

    public JooqUserRepository() {
        super(CstlUser.class, CSTL_USER);
    }

    @Override
    public List<DomainUser> findAllWithDomainAndRole() {

        SelectConditionStep<Record> records = getSelectWithRolesAndDomains();

        records.execute();

        Result<Record> result = records.getResult();

        List<DomainUser> dtos = mapUsers(result);

        return dtos;

    }

    private SelectConditionStep<Record> getSelectWithRolesAndDomains() {
        SelectConditionStep<Record> records = dsl.select().from(userTable).leftOuterJoin(UDD).on(userTable.ID.eq(UDD.USER_ID))
                .leftOuterJoin(userXroleTable).on(userTable.ID.eq(userXroleTable.USER_ID)).leftOuterJoin(domainTable)
                .on(domainTable.ID.eq(UDD.DOMAIN_ID)).where(userTable.ACTIVE.eq(true));
        return records;
    }

    private List<DomainUser> mapUsers(Result<Record> result) {
        List<DomainUser> dtos = new ArrayList<>();
        Map<Record, Result<Record>> users = result.intoGroups(userTable.fields());
        for (Entry<Record, Result<Record>> record : users.entrySet()) {
            DomainUser userDTO = mapUser(record);

            dtos.add(userDTO);
        }
        return dtos;
    }

    private DomainUser mapUser(Entry<Record, Result<Record>> record) {
        DomainUser userDTO = record.getKey().into(DomainUser.class);

        Map<Record, Result<Record>> roles = record.getValue().intoGroups(userXroleTable.fields());
        for (Record roleRecord : roles.keySet()) {
            String role = roleRecord.getValue(userXroleTable.ROLE);
            if (role != null) {
                userDTO.addRole(role);
            }
        }

        Map<Record, Result<Record>> domains = record.getValue().intoGroups(domainTable.fields());
        for (Entry<Record, Result<Record>> domainEntry : domains.entrySet()) {

            Domain domain = domainEntry.getKey().into(Domain.class);
            if (domain.getId() != null) {
                userDTO.addDomain(domain);
            }
        }
        return userDTO;
    }

    @Override
    @Transactional
    public CstlUser update(CstlUser user, List<String> roles) {

        UpdateConditionStep<CstlUserRecord> update = dsl.update(CSTL_USER).set(CSTL_USER.EMAIL, user.getEmail())
                .set(CSTL_USER.LASTNAME, user.getLastname()).set(CSTL_USER.FIRSTNAME, user.getFirstname())
                .where(CSTL_USER.LOGIN.eq(user.getLogin()));

        update.execute();

        DeleteConditionStep<UserXRoleRecord> deleteRoles = dsl.delete(USER_X_ROLE).where(USER_X_ROLE.USER_ID.eq(user.getId()));

        deleteRoles.execute();

        insertRoles(user, roles);

        return user;
    }

    @Override
    @Transactional
    public CstlUser insert(CstlUser user, List<String> roles) {

        user.setActive(true);
        CstlUserRecord newRecord = dsl.newRecord(CSTL_USER);

        CstlUserHelper.copy(user, newRecord);

        if (newRecord.store() > 0) {
            user.setId(newRecord.getId());
        }

        insertRoles(user, roles);

        return user;
    }

    private void insertRoles(CstlUser user, List<String> roles) {
        for (String role : roles) {
            InsertSetMoreStep<UserXRoleRecord> insertRole = dsl.insertInto(USER_X_ROLE).set(USER_X_ROLE.USER_ID, user.getId())
                    .set(USER_X_ROLE.ROLE, role);
            insertRole.execute();
        }
    }

    @Override
    @Transactional
    public int delete(int userId) {
        int deleteRole = deleteRole(userId);

        int removeUserFromAllDomain = domainRepository.removeUserFromAllDomain(userId);
        LOGGER.debug("Delete " + removeUserFromAllDomain + " domain references");

        LOGGER.debug("Delete " + deleteRole + " role references");

        return dsl.delete(CSTL_USER).where(CSTL_USER.ID.eq(userId)).execute();

    }

    @Override
    public int desactivate(int userId) {
        return dsl.update(Tables.CSTL_USER).set(CSTL_USER.ACTIVE, false).where(CSTL_USER.ID.eq(userId)).execute();
    }

    @Override
    public int activate(int userId) {
        return dsl.update(Tables.CSTL_USER).set(CSTL_USER.ACTIVE, true).where(CSTL_USER.ID.eq(userId)).execute();
    }

    private int deleteRole(int userId) {
        return dsl.delete(USER_X_ROLE).where(USER_X_ROLE.USER_ID.eq(userId)).execute();

    }

    @Override
    public Optional<DomainUser> findOneWithRolesAndDomains(String login) {
        return fetchUserWithRolesAndDomains(userTable.LOGIN.eq(login));
    }

    private Optional<DomainUser> fetchUserWithRolesAndDomains(Condition condition) {
        SelectConditionStep<Record> records = getSelectWithRolesAndDomains().and(condition);
        if (records.execute() > 0) {
            List<DomainUser> result = mapUsers(records.getResult());
            DomainUser domainUser = result.get(0);
            domainUser.setPassword(null);
            return Optional.of(domainUser);
        }
        return Optional.absent();
    }

    @Override
    public Optional<DomainUser> findOneWithRolesAndDomains(int id) {
        return fetchUserWithRolesAndDomains(userTable.ID.eq(id));
    }

    @Override
    public boolean isLastAdmin(int userId) {
        Record1<Integer> where = dsl.selectCount().from(CSTL_USER).join(USER_X_ROLE).onKey()
                .where(USER_X_ROLE.ROLE.eq("cstl-admin").and(CSTL_USER.ID.ne(userId))).fetchOne();
        return where.value1() == 0;
    }

    @Override
    public Optional<CstlUser> findOne(String login) {
        if (login == null)
            return Optional.absent();
        return Optional.fromNullable(dsl.select().from(CSTL_USER).where(CSTL_USER.LOGIN.eq(login)).fetchOneInto(CstlUser.class));
    }

    @Override
    public List<String> getRoles(int userId) {
        return dsl.select().from(CSTL_USER).where(USER_X_ROLE.USER_ID.eq(userId)).fetch(USER_X_ROLE.ROLE);
    }

    @Override
    public int countUser() {
        return dsl.selectCount().from(CSTL_USER).fetchOne(0, int.class);
    }

    @Override
    public boolean loginAvailable(String login) {
        return dsl.selectCount().from(CSTL_USER).where(CSTL_USER.LOGIN.eq(login)).fetchOne().value1() == 0;
    }

    @Override
    public List<CstlUser> findUsersByDomainId(int domainId) {
        return findBy(CSTL_USER.ID.in(dsl.selectDistinct(USER_X_DOMAIN_X_DOMAINROLE.USER_ID).from(USER_X_DOMAIN_X_DOMAINROLE)
                .where(USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId))));
    }

    @Override
    public Map<CstlUser, List<Domainrole>> findUsersWithDomainRoles(int domainId) {
        Map<CstlUser, List<Domainrole>> result = new LinkedHashMap<>();
        SelectConditionStep<Record> groupBy = dsl.select().from(CSTL_USER).join(USER_X_DOMAIN_X_DOMAINROLE).onKey().join(Tables.DOMAINROLE)
                .onKey().where(USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId));
        groupBy.execute();
        Map<Record, Result<Record>> intoGroups = groupBy.getResult().intoGroups(Tables.CSTL_USER.fields());
        for (Entry<Record, Result<Record>> userRecord : intoGroups.entrySet()) {
            CstlUser user = userRecord.getKey().into(CstlUser.class);

            result.put(user, userRecord.getValue().into(Domainrole.class));
        }
        return result;
    }

    @Override
    public List<CstlUser> findUsersNotInDomain(int domainId) {
        return dsl
                .select()
                .from(CSTL_USER)
                .where(CSTL_USER.ID.notIn(dsl.selectDistinct(USER_X_DOMAIN_X_DOMAINROLE.USER_ID).from(USER_X_DOMAIN_X_DOMAINROLE)
                        .where(USER_X_DOMAIN_X_DOMAINROLE.DOMAIN_ID.eq(domainId)))).and(CSTL_USER.ACTIVE.eq(true)).fetchInto(CstlUser.class);
    }

}
