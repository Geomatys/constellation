package org.constellation.database.api.repository;

import org.constellation.database.api.jooq.tables.pojos.Role;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: laurent
 * Date: 07/05/15
 * Time: 14:24
 * Geomatys
 */
public interface RoleRepository {
    List<Role> findAll();
}
