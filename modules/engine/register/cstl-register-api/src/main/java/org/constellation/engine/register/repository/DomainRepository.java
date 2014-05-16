package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Set;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.User;

public interface DomainRepository {
  
    List<Domain> findAll();

    Domain findOne(Integer id);
    
    Domain save(Domain domain);

    void update(Domain domain);

    int delete(int domainId);

    int[] addUserToDomain(String userId, int domainId, Set<String> roles);

    int removeUserFromDomain(String userId, int domainId);
    
    void addServiceToDomain(int serviceId, int domainId);
    
    int removeServiceFromDomain(int serviceId, int domainId);

    int removeUserFromAllDomain(String userId);

    int addDataToDomain(int dataId, int domainId);
    
    int removeDataFromDomain(int dataId, int domainId);

    int removeAllDataFromDomain(int i);

    int addProviderDataToDomain(String id, int activeDomainId);

    List<User> findUsers(int id);


}
