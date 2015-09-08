package org.constellation.database.impl.repository;

import org.constellation.database.api.jooq.tables.pojos.Role;
import org.constellation.database.api.jooq.tables.records.RoleRecord;
import org.constellation.database.api.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import static org.constellation.database.api.jooq.Tables.ROLE;

/**
 * Created with IntelliJ IDEA.
 * User: laurent
 * Date: 07/05/15
 * Time: 14:25
 * Geomatys
 */
@Component("cstlRoleRepository")
public class JooqRoleRepository extends AbstractJooqRespository<RoleRecord, Role> implements RoleRepository{

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public JooqRoleRepository() {
        super(Role.class, ROLE);
    }
}
