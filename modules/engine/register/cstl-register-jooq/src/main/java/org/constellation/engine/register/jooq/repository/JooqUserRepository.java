package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.USER;
import static org.constellation.engine.register.jooq.Tables.USER_X_ROLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.constellation.engine.register.User;
import org.constellation.engine.register.jooq.Tables;
import org.constellation.engine.register.jooq.tables.Domain;
import org.constellation.engine.register.jooq.tables.UserXDomainXDomainrole;
import org.constellation.engine.register.jooq.tables.UserXRole;
import org.constellation.engine.register.jooq.tables.records.UserRecord;
import org.constellation.engine.register.jooq.tables.records.UserXRoleRecord;
import org.constellation.engine.register.repository.UserRepository;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.UpdateConditionStep;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqUserRepository extends AbstractJooqRespository<UserRecord, User> implements UserRepository {

    private org.constellation.engine.register.jooq.tables.User userTable = USER.as("u");
    private UserXRole userXroleTable = Tables.USER_X_ROLE.as("uXr");
    private Domain domainTable = Tables.DOMAIN.as("d");

    private UserXDomainXDomainrole UDD = Tables.USER_X_DOMAIN_X_DOMAINROLE.as("uxdr");

    
    public JooqUserRepository() {
        super(User.class, USER);
    }
    
    
    @Override
    public List<User> all() {
        return findAll();
    }


    public List<User> findAll() {

        SelectJoinStep<Record> records = getSelectWithRolesAndDomains();

        records.execute();

        Result<Record> result = records.getResult();

        List<User> dtos = mapUsers(result);

        return dtos;

    }

    private SelectJoinStep<Record> getSelectWithRolesAndDomains() {
        SelectJoinStep<Record> records = dsl.select().from(userTable).leftOuterJoin(UDD).on(userTable.LOGIN.eq(UDD.LOGIN))
                .leftOuterJoin(userXroleTable).on(userTable.LOGIN.eq(userXroleTable.LOGIN)).leftOuterJoin(domainTable).on(domainTable.ID.eq(UDD.DOMAIN_ID));
        return records;
    }
    
    
   

    private List<User> mapUsers(Result<Record> result) {
        List<User> dtos = new ArrayList<User>();
        Map<Record, Result<Record>> users = result.intoGroups(userTable.fields());
        for (Entry<Record, Result<Record>> record : users.entrySet()) {
            User userDTO = mapUser(record);

            dtos.add(userDTO);
        }
        return dtos;
    }

    private User mapUser(Entry<Record, Result<Record>> record) {
        User userDTO = record.getKey().into(User.class);

        Map<Record, Result<Record>> roles = record.getValue().intoGroups(userXroleTable.fields());
        for (Record roleRecord : roles.keySet()) {
            String role = roleRecord.getValue(userXroleTable.ROLE);
            if (role != null) {
                userDTO.addRole(role);
            }
        }

        Map<Record, Result<Record>> domains = record.getValue().intoGroups(domainTable.fields());
        for (Entry<Record, Result<Record>> domain : domains.entrySet()) {
            
            Integer value = domain.getKey().getValue(Tables.DOMAIN.ID);
            if (value != null) {
                userDTO.addDomain(value);
            }
        }
        return userDTO;
    }

    @Override
    @Transactional
    public void update(User user) {
        
        UpdateConditionStep<UserRecord> update = dsl.update(USER).set(USER.EMAIL, user.getEmail())
                .set(USER.LASTNAME, user.getLastname()).set(USER.FIRSTNAME, user.getFirstname())
                .set(USER.PASSWORD, user.getPassword()).where(USER.LOGIN.eq(user.getLogin()));


        update.execute();
        
        DeleteConditionStep<UserXRoleRecord> deleteRoles = dsl.delete(USER_X_ROLE).where(USER_X_ROLE.LOGIN.eq(user.getLogin()));
                
        deleteRoles.execute();
        
        insertRoles(user);
        

    }

    private void insertRoles(User user) {
        for (String role : user.getRoles()) {
            InsertSetMoreStep<UserXRoleRecord> insertRole = dsl.insertInto(USER_X_ROLE).set(USER_X_ROLE.LOGIN, user.getLogin()).set(USER_X_ROLE.ROLE, role);
            insertRole.execute();
        }
    }

    @Override
    @Transactional
    public void insert(User user) {
        
        InsertSetMoreStep<UserRecord> update = dsl.insertInto(USER).set(USER.EMAIL, user.getEmail())
                .set(USER.LASTNAME, user.getLastname()).set(USER.FIRSTNAME, user.getFirstname())
                .set(USER.PASSWORD, user.getPassword()).set(USER.LOGIN, user.getLogin());

        update.execute();
        
        insertRoles(user);
        
    }

    

    @Override
    public void delete(String userId) {
        dsl.delete(USER).where(USER.LOGIN.eq(userId)).execute();
    }

   

    @Override
    public User findOneWithRolesAndDomains(String login) {
        SelectConditionStep<Record> records = getSelectWithRolesAndDomains().where(userTable.LOGIN.eq(login));
        records.execute();
        List<User> result = mapUsers(records.getResult());
        if(result.size() == 0)
            return null;
        return result.get(0);
    }

}
