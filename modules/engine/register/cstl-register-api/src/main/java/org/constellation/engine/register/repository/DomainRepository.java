package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Set;

import org.constellation.engine.register.Domain;

public interface DomainRepository {
  
    List<? extends Domain> findAll();

    Domain findOne(Integer id);
    
    Domain save(Domain domain);

    void update(Domain domain);

    int delete(int domainId);

    int[] addUserToDomain(String userId, int domainId, Set<String> roles);

    int removeUserFromDomain(String userId, int domainId);
}
