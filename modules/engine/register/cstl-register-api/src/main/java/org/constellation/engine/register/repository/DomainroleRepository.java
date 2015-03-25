package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Domainrole;
import org.constellation.engine.register.jooq.tables.pojos.Permission;

import com.google.common.base.Optional;

public interface DomainroleRepository {

    List<Domainrole> findAll();

    Map<Domainrole, List<Permission>> findAllWithPermission();
    
    Domainrole createWithPermissions(Domainrole group,List<Permission> permissions);

    Domainrole updateWithPermissions(Domainrole group, List<Permission> permissions);

    /**
     * Does not delete system entries.
     * 
     * @param id
     * @return 
     */
    int delete(int id);

    Optional<Pair<Domainrole, List<Permission>>> findOneWithPermission(int id);

    List<Permission> allPermission();

    Map<Domainrole, List<Pair<CstlUser, List<Domain>>>> findAllWithMembers();

    Map<Domainrole, List<Integer>> findAllWithPermissions(int ... serviceWriteAccessPermissionId);

    List<Domainrole> findUserDomainroles(int id, int domainId);



}
