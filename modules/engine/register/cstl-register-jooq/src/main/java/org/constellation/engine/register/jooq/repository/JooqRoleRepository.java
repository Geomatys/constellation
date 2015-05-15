package org.constellation.engine.register.jooq.repository;

import org.constellation.engine.register.jooq.tables.pojos.Role;
import org.constellation.engine.register.jooq.tables.records.RoleRecord;
import org.constellation.engine.register.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import static org.constellation.engine.register.jooq.Tables.ROLE;

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
