package org.constellation.engine.register.repository;

import com.google.common.base.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Domainrole;
import org.constellation.engine.register.Permission;
import org.constellation.engine.register.User;

import java.util.List;
import java.util.Map;

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

    Map<Domainrole, List<Pair<User, List<Domain>>>> findAllWithMembers();

    Map<Domainrole, List<Integer>> findAllWithPermissions(int ... serviceWriteAccessPermissionId);

    List<Domainrole> findUserDomainroles(int id, int domainId);



}
