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
package org.constellation.engine.register.jooq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.configuration.DataBrief;
import org.constellation.dto.Service;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JooqConfigurationService implements ConfigurationService {

    
    @Autowired
    PropertyRepository propertyRepository;
    
    @Autowired
    ServiceRepository serviceRepository;
    
    
    @Transactional
    public Object getConfiguration(String serviceType, String serviceID, String fileName, MarshallerPool pool)
            throws JAXBException, FileNotFoundException {
        final org.constellation.engine.register.Service rec = serviceRepository.findByIdentifierAndType(serviceID,
                ServiceDef.Specification.fromShortName(serviceType).name());
        if (rec != null) {
            String is = null;
            if (fileName == null) {
                is = rec.getConfig();
            } else {
                for (ServiceExtraConfig serviceExtraConfig : serviceRepository.getExtraConfig(rec.getId())) {
                    if (fileName.equals(serviceExtraConfig.getFilename()))
                        is = serviceExtraConfig.getContent();
                }

            }
            if (is != null) {
                final Unmarshaller u = pool.acquireUnmarshaller();
                final Object config = u.unmarshal(new StringReader(is));
                pool.recycle(u);
                return config;
            }
        }

        throw new FileNotFoundException("The configuration (" + fileName != null ? fileName : "default"
                + ") has not been found.");

    }

    @Override
    public boolean deleteService(String identifier, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DataBrief getData(QName name, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return propertyRepository.getValue(key, defaultValue);
    }

    @Override
    public DataBrief getDataLayer(String layerAlias, String providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isServiceConfigurationExist(String serviceType, String identifier) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getServiceIdentifiersByServiceType(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Service readServiceMetadata(String identifier, String serviceType, String language) throws JAXBException,
            IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getProviderIdentifiers() {
        // TODO Auto-generated method stub
        return null;
    }

}
