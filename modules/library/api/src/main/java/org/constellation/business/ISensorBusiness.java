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
package org.constellation.business;

import java.util.List;

import javax.xml.namespace.QName;

import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Sensor;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface ISensorBusiness {
    void linkDataToSensor(QName name, String providerId, String sensorId);

    void unlinkDataToSensor(QName name, String providerId, String sensorId) throws TargetNotFoundException;

    List<Sensor> getAll();

    List<Sensor> getChildren(Sensor sensor);

    void delete(String sensorid);

    Sensor getSensor(String sensorid);

    Sensor create(String id, String type, String parentID, String sml);

    void update(Sensor childRecord);

    List<Data> getLinkedData(Sensor record);
}
