package org.constellation.admin;

import java.util.List;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.security.*;
import org.springframework.stereotype.Component;

@Component
public class SensorBusiness {

    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private org.constellation.security.SecurityManager securityManager;

    @Inject
    private DataRepository dataRepository;
    
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

    public void linkDataToSensor(QName dataName, String providerId, String sensorIdentifier) {

        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(dataName.getLocalPart(), dataName.getNamespaceURI(), providerId);
        final Sensor sensor = sensorRepository.findByIdentifier(sensorIdentifier);
        sensorRepository.linkDataToSensor(data.getId(),sensor.getId());

    }

    public void unlinkDataToSensor(QName dataName, String providerId, String sensorIdentifier) {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(dataName.getLocalPart(), dataName.getNamespaceURI(), sensorIdentifier);
        final Sensor sensor = sensorRepository.findByIdentifier(sensorIdentifier);
        sensorRepository.unlinkDataToSensor(data.getId(),sensor.getId());
    }

    public Sensor create(final String identifier, final String type, final String parent) {
        return create(identifier,type,parent,null);
    }

    public Sensor create(final String identifier, final String type, final String parent, final String metadata) {
        Sensor sensor = new Sensor();
        sensor.setIdentifier(identifier);
        sensor.setType(type);
        sensor.setOwner(securityManager.getCurrentUserLogin());
        sensor.setParent(parent);
        sensor.setMetadata(metadata);
        return sensorRepository.create(sensor);
    }

    public void update(Sensor sensor) {
        sensorRepository.update(sensor);
    }
}
