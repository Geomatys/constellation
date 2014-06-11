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
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.print.attribute.standard.Severity;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
//import org.constellation.admin.util.IOUtilities;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.security.SecurityManager;
import org.constellation.utils.CstlMetadatas;
import org.jaxen.FunctionCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Component
public class JooqConfigurationService implements ConfigurationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private LayerRepository layerRepository;

    /**
     * Store configuration, this method is too "generic" and should be refactored/splited.
     * <br />
     * When obj id a LayerContext 
     */
    @Override
    @Transactional
    public void storeConfiguration(String serviceType, String serviceID, String fileName, Object obj,
            MarshallerPool pool, String login) {
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);

        String config = null;
        if (obj != null) {
            try {
                final StringWriter sw = new StringWriter();
                final Marshaller m = pool.acquireMarshaller();
                m.marshal(obj, sw);
                pool.recycle(m);
                config = sw.toString();
            } catch (JAXBException e) {
                throw new ConstellationPersistenceException(e);
            }

        }

       
        Service service = serviceRepository.findByIdentifierAndType(serviceID, spec.name());
        if (service == null) {
            service = new Service();
            service.setConfig(config);
            service.setDate(new Date().getTime());
            service.setType(spec.name());
            service.setOwner(login);
            service.setIdentifier(serviceID);
            
            serviceRepository.create(service);
        }
         

        if (obj instanceof LayerContext) {

            Map<String, org.constellation.engine.register.Layer> layersByKey = new HashMap<String, org.constellation.engine.register.Layer>();
            layersByKey.putAll(Maps.uniqueIndex(layerRepository.findByServiceId(service.getId()),
                    new Function<org.constellation.engine.register.Layer, String>() {

                        @Override
                        public String apply(org.constellation.engine.register.Layer layer) {
                            return generateLayerByServiceKey(layer.getService(), layer.getName());
                        }

                    }));

            // layerRepository.deleteServiceLayer(service);
            // save Layers
            LayerContext context = (LayerContext) obj;
            final List<String> layerIds = new ArrayList<>();
            for (Source source : context.getLayers()) {
                for (Layer layer : source.getInclude()) {

                    final Data data = dataRepository.findDataFromProvider(layer.getName().getNamespaceURI(), layer
                            .getName().getLocalPart(), source.getId());

                    String layerKey = generateLayerByServiceKey(service.getId(), layer.getName().getLocalPart());

                    org.constellation.engine.register.Layer storeLayer = layersByKey.get(layerKey);

                    if (storeLayer == null) {
                        storeLayer = new org.constellation.engine.register.Layer();
                        storeLayer.setDate(new Date().getTime());
                        storeLayer.setService(service.getId());
                    }

                    storeLayer.setOwner(login);
                    storeLayer.setAlias(layer.getAlias());
                    storeLayer.setNamespace(layer.getName().getNamespaceURI());
                    storeLayer.setName(layer.getName().getLocalPart());
                    storeLayer.setData(data.getId());
                    storeLayer.setService(service.getId());

                    if (layersByKey.containsKey(layerKey)) {
                        layerRepository.update(storeLayer);
                        layersByKey.remove(layerKey);
                    } else {
                        layerRepository.save(storeLayer);
                    }

                    if (data.hasIsoMetadata()) {
                        layerIds.add(data.getMetadataId());
                    }
                }
            }
            for (Entry<String, org.constellation.engine.register.Layer> entry : layersByKey.entrySet()) {
                layerRepository.delete(entry.getValue());
            }
            if (service.hasIsoMetadata()) {
                try {
                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(service.getMetadata());
                    CstlMetadatas.updateServiceMetadataLayer(servMeta, layerIds);
                    final String srm = MetadataIOUtils.marshallMetadataToString(servMeta);
                    serviceRepository.updateIsoMetadata(service, servMeta.getFileIdentifier(), srm.toString());
                } catch (JAXBException e) {
                    throw new ConstellationPersistenceException(e);
                }
            }
        }

        if (fileName == null) {
            service.setConfig(config);
            serviceRepository.updateConfig(service);
        } else {
            serviceRepository.updateExtraFile(service, fileName, config);
        }

    }

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
                ServiceExtraConfig serviceExtraConfig = serviceRepository.getExtraConfig(rec.getId(), fileName);
                if (serviceExtraConfig != null) {
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
        return null;
    }

    @Override
    public List<String> getProviderIdentifiers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteData(String namespaceURI, String localPart, String providerIdentifier) {
        Provider provider = providerRepository.findByIdentifie(providerIdentifier);
        if (provider == null) {

        } else {
            dataRepository.delete(namespaceURI, localPart, provider.getId());
        }

    }

    @Override
    public void deleteProvider(String providerID) {
        providerRepository.deleteByIdentifier(providerID);

    }

    @Override
    public List<Provider> findProvidersByImpl(String serviceName) {
        return providerRepository.findByImpl(serviceName);
    }

    @Override
    public Service findServiceByIdentifierAndType(String serviceID, String serviceType) {

        return serviceRepository.findByIdentifierAndType(serviceID, serviceType);
    }

    private String generateLayerByServiceKey(int serviceId, String layerName) {
        return serviceId + "-" + layerName;
    }

}
