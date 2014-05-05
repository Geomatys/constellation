package org.constellation.engine.register.jooq.repository;

import static org.constellation.engine.register.jooq.Tables.SERVICE;

import java.util.List;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.jooq.tables.records.ServiceRecord;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.stereotype.Component;

@Component
public class JooqServiceRepository extends AbstractJooqRespository<ServiceRecord, Service> implements
        ServiceRepository {

    
    public JooqServiceRepository() {
        super(Service.class, SERVICE);
    }
    

    @Override
    public List<? extends Service> findByDataId(int dataId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Service findByIdentifierAndType(String id, String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(Integer id) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> findIdentifiersByType(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceMetaData findMetaDataForLangByIdentifierAndType(String identifier, String serviceType, String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ServiceExtraConfig> getExtraConfig(int id) {
//        return dsl.select().from(SERVICE_EXTRA_CONFIG).where(SERVICE_EXTRA_CONFIG.ID.eq(id)).fetchInto(ServiceE)
//            
//        }
        
        return null;
    }

}
