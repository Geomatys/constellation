package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.DomainRole;

public interface DomainRoleRepository {

    List<DomainRole> findAll();
    
    DomainRole save(DomainRole group);

    DomainRole update(DomainRole group);

    void delete(String name);

    DomainRole findOneWithPermission(String name);
    
}
