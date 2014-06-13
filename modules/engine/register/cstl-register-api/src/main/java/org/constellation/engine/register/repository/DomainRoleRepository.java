package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.DomainRole;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.User;

public interface DomainRoleRepository {

    List<DomainRole> findAll();

    DomainRole save(DomainRole group);

    DomainRole update(DomainRole group);

    /**
     * Does not delete system entries.
     * 
     * @param id
     * @return 
     */
    int delete(int id);

    DomainRole findOneWithPermission(int id);

    List<Permission> allPermission();

    Map<DomainRole, List<Pair<User, List<Domain>>>> findAllWithMembers();

    Map<DomainRole, List<Integer>> findAllWithPermissions(int ... serviceWriteAccessPermissionId);

    List<DomainRole> findUserDomainRoles(int id, int domainId);

}
