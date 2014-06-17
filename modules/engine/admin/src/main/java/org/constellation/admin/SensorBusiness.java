package org.constellation.admin;

import java.util.List;
import javax.inject.Inject;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;
import org.constellation.engine.register.repository.SensorRepository;
import org.springframework.stereotype.Component;

@Component
public class SensorBusiness {

    @Inject
    SensorRepository sensorRepository;
    
    public Sensor getSensor(final String id) {
        return sensorRepository.findByIdentifier(id);
    }
    
    public List<Sensor> getAll() {
        return sensorRepository.findAll();
    }
    
    public List<Data> getLinkedData(final Sensor sensor){
        return sensorRepository.getLinkedDatas(sensor);
    }
    
    public List<Sensor> getChildren(final Sensor sensor) {
        return sensorRepository.getChildren(sensor);
    }
    
    public void delete(final String identifier) {
        sensorRepository.delete(identifier);
    }
}
