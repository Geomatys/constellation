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

import java.util.List;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.constellation.business.ISensorBusiness;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Sensor;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;

@Component
@Primary
public class SensorBusiness implements ISensorBusiness {

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private org.constellation.security.SecurityManager securityManager;

    @Inject
    private DataRepository dataRepository;

    @Override
    public Sensor getSensor(final String id) {
        return sensorRepository.findByIdentifier(id);
    }

    @Override
    public List<Sensor> getAll() {
        return sensorRepository.findAll();
    }

    @Override
    public List<Data> getLinkedData(final Sensor sensor){
        return sensorRepository.getLinkedDatas(sensor);
    }

    @Override
    public List<Sensor> getChildren(final Sensor sensor) {
        return sensorRepository.getChildren(sensor);
    }

    @Override
    @Transactional
    public void delete(final String identifier) {
        sensorRepository.delete(identifier);
    }

    @Override
    @Transactional
    public void linkDataToSensor(QName dataName, String providerId, String sensorIdentifier) {
        final Data data = dataRepository.findDataFromProvider(dataName.getNamespaceURI(), dataName.getLocalPart(), providerId);
        final Sensor sensor = sensorRepository.findByIdentifier(sensorIdentifier);
        sensorRepository.linkDataToSensor(data.getId(),sensor.getId());
    }

    /**
     * Proceed to remove the link between data and sensor.
     *
     * @param dataName given data name to find the data instance.
     * @param providerId given provider identifier for data.
     * @param sensorIdentifier given sensor identifier that will be unlinked.
     */
    @Override
    @Transactional
    public void unlinkDataToSensor(final QName dataName,
                                   final String providerId,
                                   final String sensorIdentifier) throws TargetNotFoundException {
        final Data data = dataRepository.findDataFromProvider(
                dataName.getNamespaceURI(),
                dataName.getLocalPart(),
                providerId);
        final Sensor sensor = sensorRepository.findByIdentifier(sensorIdentifier);
        if(data == null){
            throw new TargetNotFoundException("Cannot unlink data to sensor," +
                    " because target data is not found for" +
                    " name : "+dataName.getLocalPart()+" and provider : "+providerId);
        }
        if(sensor == null){
            throw new TargetNotFoundException("Cannot unlink data to sensor," +
                    " because target sensor is not found for" +
                    " sensorIdentifier : "+sensorIdentifier);
        }
        sensorRepository.unlinkDataToSensor(data.getId(),sensor.getId());
    }

    public Sensor create(final String identifier, final String type, final String parent) {
        return create(identifier,type,parent,null);
    }

    @Override
    @Transactional
    public Sensor create(final String identifier, final String type, final String parent, final String metadata) {
        Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
        Sensor sensor = new Sensor();
        sensor.setIdentifier(identifier);
        sensor.setType(type);
        if(user.isPresent()) {
            sensor.setOwner(user.get().getId());
        }
        sensor.setParent(parent);
        sensor.setMetadata(metadata);
        return sensorRepository.create(sensor);
    }

    @Override
    @Transactional
    public void update(Sensor sensor) {
        sensorRepository.update(sensor);
    }
}
