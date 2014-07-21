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

package org.constellation.map.configuration;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.AddLayer;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.User;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.map.factory.MapFactory;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import javax.imageio.spi.ServiceRegistry;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class LayerBusiness {
    
    @Inject
    private UserRepository userRepository;
    @Autowired
    private StyleRepository styleRepository;
    @Autowired
    private LayerRepository layerRepository;
    @Autowired
    private DataRepository dataRepository;
    @Autowired
    private ProviderRepository providerRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private org.constellation.security.SecurityManager securityManager;
    
    public void add(final AddLayer addLayerData) throws ConfigurationException {
        final String name        = addLayerData.getLayerId();
        final String namespace   = addLayerData.getLayerNamespace();
        final String providerId  = addLayerData.getProviderId();
        final String alias       = addLayerData.getLayerAlias();
        final String serviceId   = addLayerData.getServiceId();
        final String serviceType = addLayerData.getServiceType();
        add(name, namespace, providerId, alias, serviceId, serviceType, null);
    }
    
    public void add(final String name, String namespace, final String providerId, final String alias,
            final String serviceId, final String serviceType, final org.constellation.configuration.Layer config) throws ConfigurationException {
        
        final Service service = serviceRepository.findByIdentifierAndType(serviceId, serviceType.toLowerCase());
        
        if (service !=null) {
            
            // look for layer namespace
            if (namespace == null) {
                final DataProvider provider = DataProviders.getInstance().getProvider(providerId);
                if (provider != null) {
                    namespace = ProviderParameters.getNamespace(provider);
                }
            }

            final Data data = dataRepository.findDataFromProvider(namespace, name, providerId);
            boolean update = true;
            Layer layer = layerRepository.findByServiceIdAndLayerName(service.getId(), name, namespace);
            if (layer == null) {
                update = false;
                layer = new Layer();
            }
            layer.setName(name);
            layer.setNamespace(namespace);
            layer.setAlias(alias);
            layer.setService(service.getId());
            layer.setData(data.getId());
            layer.setDate(System.currentTimeMillis());
            Optional<User> user = userRepository.findOne(securityManager.getCurrentUserLogin());
            if(user.isPresent()) {
                layer.setOwner(user.get().getId());
            }
            final String configXml = getStringFromLayerConfig(config);
            layer.setConfig(configXml);
            
            int layerID;
            if (!update) {
                layerID = layerRepository.save(layer).getId();
            } else {
                layerID = layerRepository.update(layer);
            }
            
            for (int styleID : styleRepository.getStyleIdsForData(data.getId())) {
                styleRepository.linkStyleToLayer(styleID, layerID);
            }
            //style
            
            //update service ISO metadata
//            if (service.getMetadata() != null) {
//                try {
//                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(service.getMetadata());
//                    CstlMetadatas.addServiceMetadataLayer(servMeta, name);
//                    final String srm = MetadataIOUtils.marshallMetadataToString(servMeta);
//                    serviceRepository.updateIsoMetadata(service, servMeta.getFileIdentifier(), srm);
//                } catch (JAXBException e) {
//                    throw new ConstellationPersistenceException(e);
//                }
//            }
            
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + serviceId);
        }
    }
    
    public void remove(final String spec, final String serviceId, final String name, final String namespace) throws ConfigurationException {
        final Service service = serviceRepository.findByIdentifierAndType(serviceId, spec.toLowerCase());
        if (service != null) {
            final Layer layer = layerRepository.findByServiceIdAndLayerName(service.getId(), name, namespace);
            if (layer != null) {
                layerRepository.delete(layer.getId());
            } else {
                throw new TargetNotFoundException("Unable to find a layer: {" + namespace + "}" + name);
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + serviceId);
        }
    }
    
    public void removeForService(final String spec, final String serviceId) throws ConfigurationException {
        final Service service = serviceRepository.findByIdentifierAndType(serviceId, spec.toLowerCase());
        if (service != null) {
            final List<Layer> layers = layerRepository.findByServiceId(service.getId());
            for (Layer layer : layers) {
                layerRepository.delete(layer.getId());
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + serviceId);
        }
    }
    
    public void removeAll() {
        final List<Layer> layers = layerRepository.findAll();
        for (Layer layer : layers) {
            layerRepository.delete(layer.getId());
        }
    }
    
    public List<org.constellation.configuration.Layer> getLayers(final String spec, final String identifier, final String login) throws ConfigurationException {
        final List<org.constellation.configuration.Layer> response = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(identifier, spec.toLowerCase());
        
        if (service != null) {
            final LayerContext context = readMapConfiguration(service.getConfig());
            final MapFactory mapfactory = getMapFactory(context.getImplementation());
            final LayerSecurityFilter securityFilter = mapfactory.getSecurityFilter();
            final List<Layer> layers   = layerRepository.findByServiceId(service.getId());
            for (Layer layer : layers) {
                final Data data          = dataRepository.findById(layer.getData());
                final Provider provider  = providerRepository.findOne(data.getProvider());
                final QName name         = new QName(layer.getNamespace(), layer.getName());
                final List<Style> styles = styleRepository.findByLayer(layer);

                org.constellation.configuration.Layer layerConfig = readLayerConfiguration(layer.getConfig());
                if (securityFilter.allowed(login, name)) {
                    if (layerConfig == null) {
                        layerConfig = new org.constellation.configuration.Layer(name);
                    }
                    layerConfig.setId(layer.getId());

                    // override with table values (TODO remove)
                    layerConfig.setAlias(layer.getAlias());
                    layerConfig.setDate(new Date(layer.getDate()));
                    layerConfig.setOwner(layer.getOwner());
                    layerConfig.setProviderID(provider.getIdentifier());
                    layerConfig.setProviderType(provider.getType());
                    
                    // TODO layerDto.setAbstrac();
                    // TODO layerDto.setAttribution(null);
                    // TODO layerDto.setAuthorityURL(null);
                    // TODO layerDto.setCrs(null);
                    // TODO layerDto.setDataURL(null);
                    // TODO layerDto.setDimensions(null);
                    // TODO layerDto.setFilter(null);
                    // TODO layerDto.setGetFeatureInfoCfgs(null);
                    // TODO layerDto.setKeywords();
                    // TODO layerDto.setMetadataURL(null);
                    // TODO layerDto.setOpaque(Boolean.TRUE);
                    

                    for (Style style : styles) {
                        final Provider styleProvider = providerRepository.findOne(style.getProvider());
                        DataReference styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, styleProvider.getIdentifier(), style.getName());
                        layerConfig.getStyles().add(styleProviderReference);
                    }

                     // TODO layerDto.setTitle(null);
                     // TODO layerDto.setVersion();
                    response.add(layerConfig);
                }
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + identifier);
        }
        return response;
    }
    
    private LayerContext readMapConfiguration(final String xml) throws ConfigurationException {
        try {
            if (xml != null) {
                final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final Object config = u.unmarshal(new StringReader(xml));
                GenericDatabaseMarshallerPool.getInstance().recycle(u);
                return (LayerContext) config;
            }
            return null;
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    private org.constellation.configuration.Layer readLayerConfiguration(final String xml) throws ConfigurationException {
        try {
            if (xml != null) {
                final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final Object config = u.unmarshal(new StringReader(xml));
                GenericDatabaseMarshallerPool.getInstance().recycle(u);
                return (org.constellation.configuration.Layer) config;
            }
            return null;
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    /**
     * Select the good CSW factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private MapFactory getMapFactory(final DataSourceType type) {
        final Iterator<MapFactory> ite = ServiceRegistry.lookupProviders(MapFactory.class);
        while (ite.hasNext()) {
            MapFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No Map factory has been found for type:" + type);
    }
    
    private String getStringFromLayerConfig(final org.constellation.configuration.Layer obj) {
        String config = null;
        if (obj != null) {
            try {
                final StringWriter sw = new StringWriter();
                final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                m.marshal(obj, sw);
                GenericDatabaseMarshallerPool.getInstance().recycle(m);
                config = sw.toString();
            } catch (JAXBException e) {
                throw new ConstellationPersistenceException(e);
            }
        }
        return config;
    }
}
