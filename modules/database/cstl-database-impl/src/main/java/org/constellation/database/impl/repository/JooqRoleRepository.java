package org.constellation.database.impl.repository;

import org.constellation.database.api.jooq.tables.pojos.Role;
import org.constellation.database.api.jooq.tables.records.RoleRecord;
import org.constellation.database.api.repository.RoleRepository;
import org.springframework.stereotype.Component;


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

    public JooqRoleRepository() {
        super(Role.class, ROLE);
    }
}
