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

import static org.constellation.engine.register.jooq.Tables.CSTL_USER;
import static org.constellation.engine.register.jooq.Tables.USER_X_ROLE;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.constellation.engine.register.UserWithRole;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.records.CstlUserRecord;
import org.constellation.engine.register.jooq.tables.records.UserXRoleRecord;
import org.constellation.engine.register.repository.UserRepository;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.UpdateConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;

@Component("cstlUserRepository")
public class JooqUserRepository extends
		AbstractJooqRespository<CstlUserRecord, CstlUser> implements
		UserRepository {

	private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles
			.lookup().lookupClass());

	public JooqUserRepository() {
		super(CstlUser.class, CSTL_USER);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CstlUser update(CstlUser user, List<String> roles) {

		UpdateConditionStep<CstlUserRecord> update = dsl.update(CSTL_USER)
				.set(CSTL_USER.EMAIL, user.getEmail())
				.set(CSTL_USER.LASTNAME, user.getLastname())
				.set(CSTL_USER.FIRSTNAME, user.getFirstname())
				.where(CSTL_USER.LOGIN.eq(user.getLogin()));

		update.execute();

		DeleteConditionStep<UserXRoleRecord> deleteRoles = dsl.delete(
				USER_X_ROLE).where(USER_X_ROLE.USER_ID.eq(user.getId()));

		deleteRoles.execute();

		insertRoles(user, roles);

		return user;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public CstlUser insert(CstlUser user, List<String> roles) {

		user.setActive(true);
		CstlUserRecord newRecord = dsl.newRecord(CSTL_USER);

		newRecord.from(user);

		if (newRecord.store() > 0) {
			user.setId(newRecord.getId());
		}

		insertRoles(user, roles);

		return user;
	}

	private void insertRoles(CstlUser user, List<String> roles) {
		for (String role : roles) {
			InsertSetMoreStep<UserXRoleRecord> insertRole = dsl
					.insertInto(USER_X_ROLE)
					.set(USER_X_ROLE.USER_ID, user.getId())
					.set(USER_X_ROLE.ROLE, role);
			insertRole.execute();
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int delete(int userId) {
		int deleteRole = deleteRole(userId);

		LOGGER.debug("Delete " + deleteRole + " role references");

		return dsl.delete(CSTL_USER).where(CSTL_USER.ID.eq(userId)).execute();

	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int desactivate(int userId) {
		return dsl.update(Tables.CSTL_USER).set(CSTL_USER.ACTIVE, false)
				.where(CSTL_USER.ID.eq(userId)).execute();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int activate(int userId) {
		return dsl.update(Tables.CSTL_USER).set(CSTL_USER.ACTIVE, true)
				.where(CSTL_USER.ID.eq(userId)).execute();
	}

	private int deleteRole(int userId) {
		return dsl.delete(USER_X_ROLE).where(USER_X_ROLE.USER_ID.eq(userId))
				.execute();

	}

	@Override
	public boolean isLastAdmin(int userId) {
		Record1<Integer> where = dsl
				.selectCount()
				.from(CSTL_USER)
				.join(USER_X_ROLE)
				.onKey()
				.where(USER_X_ROLE.ROLE.eq("cstl-admin").and(
						CSTL_USER.ID.ne(userId))).fetchOne();
		return where.value1() == 0;
	}

	@Override
	public Optional<CstlUser> findOne(String login) {
		if (login == null)
			return Optional.absent();
		return Optional.fromNullable(dsl.select().from(CSTL_USER)
				.where(CSTL_USER.LOGIN.eq(login)).fetchOneInto(CstlUser.class));
	}

	@Override
	public Optional<CstlUser> findById(Integer id) {
		if (id == null)
			return Optional.absent();
		return Optional.fromNullable(dsl.select().from(CSTL_USER)
				.where(CSTL_USER.ID.eq(id)).fetchOneInto(CstlUser.class));
	}

	@Override
	public List<String> getRoles(int userId) {
		return dsl.select().from(CSTL_USER)
				.where(USER_X_ROLE.USER_ID.eq(userId)).fetch(USER_X_ROLE.ROLE);
	}

	@Override
	public Optional<UserWithRole> findOneWithRole(String name) {
		Map<CstlUserRecord, Result<Record>> fetchGroups = dsl.select()
				.from(CSTL_USER).join(Tables.USER_X_ROLE).onKey()
				.where(CSTL_USER.LOGIN.eq(name)).fetchGroups(CSTL_USER);

		if (fetchGroups.isEmpty()) {
			return Optional.absent();
		}

		List<UserWithRole> users = mapUserWithRole(fetchGroups);
		return Optional.of(users.get(0));

	}

	private List<UserWithRole> mapUserWithRole(
			Map<CstlUserRecord, Result<Record>> fetchGroups) {

		List<UserWithRole> ret = new ArrayList<>();

		for (Map.Entry<CstlUserRecord, Result<Record>> e : fetchGroups
				.entrySet()) {
			UserWithRole userWithRole = e.getKey().into(UserWithRole.class);
			List<String> roles = e.getValue()
					.getValues(Tables.USER_X_ROLE.ROLE);
			userWithRole.setRoles(roles);
			ret.add(userWithRole);
		}

		return ret;
	}

	@Override
	public int countUser() {
		return dsl.selectCount().from(CSTL_USER).fetchOne(0, int.class);
	}

	@Override
	public boolean loginAvailable(String login) {
		return dsl.selectCount().from(CSTL_USER)
				.where(CSTL_USER.LOGIN.eq(login)).fetchOne().value1() == 0;
	}

}
