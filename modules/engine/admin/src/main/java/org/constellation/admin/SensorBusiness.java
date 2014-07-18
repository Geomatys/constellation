/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.admin;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Sensor;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import java.util.List;

@Component
public class SensorBusiness {

    @Inject
    private UserRepository userRepository;
    
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
        User user = userRepository.findOne(securityManager.getCurrentUserLogin());
        Sensor sensor = new Sensor();
        sensor.setIdentifier(identifier);
        sensor.setType(type);
        sensor.setOwner(user.getId());
        sensor.setParent(parent);
        sensor.setMetadata(metadata);
        return sensorRepository.create(sensor);
    }

    public void update(Sensor sensor) {
        sensorRepository.update(sensor);
    }
}
