package org.constellation.database.api.repository;

import java.util.List;

import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Sensor;
import org.constellation.database.api.pojo.SensorReference;

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

    public boolean existsById(int sensorId);

    public boolean existsByIdentifier(String sensorIdentifier);

    public List<SensorReference> fetchByDataId(int dataId);
}
