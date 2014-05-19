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
package org.constellation.engine.register.jpa.component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConfigurationJpaService implements ConfigurationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ProviderRepository providerRepository;

    
  
    @Override
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
                for (ServiceExtraConfig serviceExtraConfig : rec.getExtraConfig()) {
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
        Service service = serviceRepository.findByIdentifierAndType(identifier, name);
        if (service == null)
            return false;
        serviceRepository.delete(service.getId());
        return true;
    }

    @Override
    @Transactional
    public DataBrief getData(QName name, String providerId) {
        Data data = dataRepository.findByNameAndNamespaceAndProviderId(name.getLocalPart(), name.getNamespaceURI(),
                providerId);

        try {
            return getDataBrief(data);
        } catch (SQLException e) {
            LOGGER.warn("error when try to read data", e);
        }
        return null;
    }

    /**
     * create a {@link org.constellation.configuration.DataBrief} with style
     * link and service link.
     * 
     * @param session
     *            current {@link org.constellation.admin.dao.Session} used
     * @param record
     *            data found
     * @return a {@link org.constellation.configuration.DataBrief} with all
     *         informations linked.
     * @throws SQLException
     *             if they have an error when search style, service or provider.
     */
    private DataBrief getDataBrief(Data data) throws SQLException {

        final List<Style> styleRecords = data.getStyles();
        final List<? extends org.constellation.engine.register.Service> serviceRecords = serviceRepository
                .findByDataId(data.getId());

        final DataBrief db = new DataBrief();
        // FIXME should never be null
        if (data.getOwner() != null)
            db.setOwner(data.getOwner().getLogin());
        db.setName(data.getName());
        db.setNamespace(data.getNamespace());
        db.setDate(new Date(data.getDate()));
        db.setProvider(data.getProvider().getIdentifier());
        db.setType(data.getType().toString());

        final List<StyleBrief> styleBriefs = new ArrayList<>(0);
        for (Style styleRecord : styleRecords) {
            final StyleBrief sb = new StyleBrief();
            sb.setType(styleRecord.getType().toString());
            sb.setProvider(styleRecord.getProvider().getIdentifier());
            sb.setDate(new Date(styleRecord.getDate()));
            sb.setName(styleRecord.getName());
            // FIXME should never be null
            if (styleRecord.getOwner() != null) {
                sb.setOwner(styleRecord.getOwner().getLogin());
            }
            styleBriefs.add(sb);
        }
        db.setTargetStyle(styleBriefs);

        final List<ServiceProtocol> serviceProtocols = new ArrayList<>(0);
        for (org.constellation.engine.register.Service serviceRecord : serviceRecords) {
            final List<String> protocol = new ArrayList<>(0);
            protocol.add(Specification.valueOf(serviceRecord.getType()).fullName);
            final ServiceProtocol sp = new ServiceProtocol(serviceRecord.getIdentifier(), protocol);
            serviceProtocols.add(sp);
        }
        db.setTargetService(serviceProtocols);

        return db;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Property property = propertyRepository.findOne(key);
        if (property == null)
            return defaultValue;
        return property.getValue();
    }

    @Override
    public DataBrief getDataLayer(String layerAlias, String providerId) {
        Data data = dataRepository.fromLayer(layerAlias, providerId);
        try {
            return getDataBrief(data);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isServiceConfigurationExist(String serviceType, String identifier) {
        return serviceRepository.findByIdentifierAndType(identifier, serviceType) != null;
    }

    @Override
    public List<String> getServiceIdentifiersByServiceType(String name) {
        return serviceRepository.findIdentifiersByType(name);
    }

    // TODO check this code with ConfigurationEngine
    @Override
    public org.constellation.dto.Service readServiceMetadata(String identifier, String serviceType, String language)
            throws JAXBException, IOException {
        ServiceMetaData serviceMetaData = serviceRepository.findMetaDataForLangByIdentifierAndType(identifier,
                serviceType, language);
        if (serviceMetaData != null) {
            final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final org.constellation.dto.Service config = (org.constellation.dto.Service) u.unmarshal(new StringReader(
                    serviceMetaData.getContent()));
            GenericDatabaseMarshallerPool.getInstance().recycle(u);
            return config;
        } else {
            final InputStream in = Util
                    .getResourceAsStream("org/constellation/xml/" + serviceType + "Capabilities.xml");
            if (in != null) {
                final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final org.constellation.dto.Service metadata = (org.constellation.dto.Service) u.unmarshal(in);
                GenericDatabaseMarshallerPool.getInstance().recycle(u);
                in.close();
                metadata.setIdentifier(identifier);
                return metadata;
            } else {
                throw new IOException("Unable to find the capabilities skeleton from resource.");
            }
        }

    }

    @Override
    public List<String> getProviderIdentifiers() {
        List<String> result = new ArrayList<String>();
        for (Provider provider : providerRepository.findAll()) {
            result.add(provider.getIdentifier());
        }
        return result;
    }

  

}
