/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
