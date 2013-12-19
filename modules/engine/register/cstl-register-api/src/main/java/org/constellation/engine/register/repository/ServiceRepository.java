package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceMetaData;

public interface ServiceRepository {

    List<? extends Service> findAll();
    
    List<? extends Service> findByDataId(int dataId);
    
    Service findByIdentifierAndType(String id, String type);

    void delete(Integer id);

    List<String> findIdentifiersByType(String type);

    ServiceMetaData findMetaDataForLangByIdentifierAndType(String identifier, String serviceType, String language);
}
