package org.constellation.engine.register.repository;

import java.util.List;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;

public interface SensorRepository {

    Sensor findByIdentifier(String identifier);
    
    List<String> getLinkedSensors(Data data);

    List<Data> getLinkedDatas(Sensor sensor);
    
    List<Sensor> getChildren(Sensor sensor);

    List<Sensor> findAll();

    void delete(String identifier);
}
