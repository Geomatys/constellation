package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Sensor;

public interface SensorRepository {

    public Sensor findByIdentifier(String identifier);
    
    public List<String> getLinkedSensors(Data data);

    public List<Data> getLinkedDatas(Sensor sensor);
    
    public List<Sensor> getChildren(Sensor sensor);

    public List<Sensor> findAll();

    public void delete(String identifier);

    public void linkDataToSensor(Integer dataId, Integer sensorId);

    public void unlinkDataToSensor(Integer dataId, Integer sensorId);

    public Sensor create(Sensor sensor);

    public void update(Sensor sensor);
}
