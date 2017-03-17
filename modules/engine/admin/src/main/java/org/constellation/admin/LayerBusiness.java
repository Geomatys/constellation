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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.google.common.base.Optional;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.LayerSummary;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.AddLayer;
import org.constellation.database.api.ConstellationPersistenceException;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Layer;
import org.constellation.database.api.jooq.tables.pojos.Provider;
import org.constellation.database.api.jooq.tables.pojos.Service;
import org.constellation.database.api.jooq.tables.pojos.Style;
import org.constellation.database.api.repository.DataRepository;
import org.constellation.database.api.repository.LayerRepository;
import org.constellation.database.api.repository.ProviderRepository;
import org.constellation.database.api.repository.ServiceRepository;
import org.constellation.database.api.repository.StyleRepository;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ws.MapFactory;
import org.constellation.ws.LayerSecurityFilter;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component("cstlLayerBusiness")
@Primary
public class LayerBusiness implements ILayerBusiness {
    
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
    @Inject
    private IDataBusiness dataBusiness;

    @Override
    @Transactional
    public void add(final AddLayer addLayerData) throws ConfigurationException {
        final String name        = addLayerData.getLayerId();
        // Prevents adding empty layer namespace, put null instead
        final String namespace   = (addLayerData.getLayerNamespace() != null && addLayerData.getLayerNamespace().isEmpty()) ? null : addLayerData.getLayerNamespace();
        final String providerId  = addLayerData.getProviderId();
        final String alias       = addLayerData.getLayerAlias();
        final String serviceId   = addLayerData.getServiceId();
        final String serviceType = addLayerData.getServiceType();
        add(name, namespace, providerId, alias, serviceId, serviceType, null);
    }

    @Override
    @Transactional
    public void add(final String name, String namespace, final String providerId, final String alias,
            final String serviceId, final String serviceType, final org.constellation.configuration.Layer config) throws ConfigurationException {
        
        final Service service = serviceRepository.findByIdentifierAndType(serviceId, serviceType.toLowerCase());
        
        if (service !=null) {

            if (namespace != null && namespace.isEmpty()) {
                // Prevents adding empty layer namespace, put null instead
                namespace = null;
            }

            // look for layer namespace
            if (namespace == null) {
                final DataProvider provider = DataProviders.getInstance().getProvider(providerId);
                if (provider != null) {
                    namespace = ProviderParameters.getNamespace(provider);
                }
            }

            final Data data = dataRepository.findDataFromProvider(namespace, name, providerId);
            if(data == null) {
                throw new TargetNotFoundException("Unable to find data for namespace:" + namespace+" name:"+name+" provider:"+providerId);
            }
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
            Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
            if(user.isPresent()) {
                layer.setOwner(user.get().getId());
            }
            final String configXml = getStringFromLayerConfig(config);
            layer.setConfig(configXml);
            
            int layerID;
            if (!update) {
                layerID = layerRepository.save(layer).getId();
            } else {
                layerRepository.update(layer);
                layerID = layer.getId();
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

    @Override
    @Transactional
    public void updateLayerTitle(LayerSummary layer) throws ConfigurationException {
        layerRepository.updateLayerTitle(layer);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
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

    @Override
    @Transactional
    public void removeAll() {
        final List<Layer> layers = layerRepository.findAll();
        for (Layer layer : layers) {
            layerRepository.delete(layer.getId());
        }
    }

    /**
     * Returns list of {@link Layer} for given style id.
     *
     * @param styleId the given style id.
     * @return the list of {@link Data}.
     */
    @Override
    public List<Layer> findByStyleId(final Integer styleId) {
        return layerRepository.getLayersByLinkedStyle(styleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LayerSummary> getLayerRefFromStyleId(final Integer styleId) {
        final List<LayerSummary> sumLayers = new ArrayList<>();
        final List<Layer> layers = layerRepository.getLayersRefsByLinkedStyle(styleId);
        for(final Layer lay : layers) {
            final LayerSummary layerSummary = new LayerSummary();
            layerSummary.setId(lay.getId());
            layerSummary.setName(lay.getName());
            sumLayers.add(layerSummary);
        }
        return sumLayers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LayerSummary> getLayerSummaryFromStyleId(final Integer styleId) {
        final List<LayerSummary> sumLayers = new ArrayList<>();
        final List<Layer> layers = findByStyleId(styleId);
        for(final Layer lay : layers){
            final QName fullName = new QName(lay.getNamespace(), lay.getName());
            final Data data = dataRepository.findById(lay.getData());
            final DataBrief db = dataBusiness.getDataBrief(fullName, data.getProvider());
            final LayerSummary layerSummary = new LayerSummary();
            layerSummary.setId(lay.getId());
            layerSummary.setName(data.getName());
            layerSummary.setNamespace(data.getNamespace());
            layerSummary.setAlias(lay.getAlias());
            layerSummary.setTitle(lay.getTitle());
            layerSummary.setType(db.getType());
            layerSummary.setSubtype(db.getSubtype());
            layerSummary.setDate(new Date(lay.getDate()));
            layerSummary.setOwner(db.getOwner());
            layerSummary.setProvider(db.getProvider());
            sumLayers.add(layerSummary);
        }
        return sumLayers;
    }

    @Override
    public List<org.constellation.configuration.Layer> getLayers(final String serviceType, final String serviceName, final String login) throws ConfigurationException {
        final List<org.constellation.configuration.Layer> response = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(serviceName, serviceType.toLowerCase());

        if (service != null) {
            final LayerContext context = readMapConfiguration(service.getConfig());
            final MapFactory mapfactory = getMapFactory(context.getImplementation());
            final LayerSecurityFilter securityFilter = mapfactory.getSecurityFilter();
            final List<Layer> layers   = layerRepository.findByServiceId(service.getId());
            for (Layer layer : layers) {
                org.constellation.configuration.Layer config = toLayerConfig(login, securityFilter, layer);
                if (config != null) {
                    response.add(config);
                }
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + serviceName);
        }
        return response;
    }

    /**
     * Get a single layer from service spec and identifier and layer name and namespace.
     *
     * @param spec service type
     * @param identifier service identifier
     * @param name layer name
     * @param namespace layer namespace
     * @param login login for security check
     * @return org.constellation.configuration.Layer
     * @throws ConfigurationException
     */
    @Override
    public org.constellation.configuration.Layer getLayer(final String spec, final String identifier, final String name,
                                                          final String namespace, final String login) throws ConfigurationException {
        final Service service = serviceRepository.findByIdentifierAndType(identifier, spec.toLowerCase());

        if (service != null) {
            final LayerContext context = readMapConfiguration(service.getConfig());
            final MapFactory mapfactory = getMapFactory(context.getImplementation());
            final LayerSecurityFilter securityFilter = mapfactory.getSecurityFilter();
            Layer layer = (namespace != null && !namespace.isEmpty())?
                    layerRepository.findByServiceIdAndLayerName(service.getId(), name, namespace) :
                    layerRepository.findByServiceIdAndLayerName(service.getId(), name);
            org.constellation.configuration.Layer layerConfig = toLayerConfig(login, securityFilter, layer);
            if (layerConfig != null) {
                return layerConfig;
            } else {
                throw new ConfigurationException("Not allowed to see this layer.");
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + identifier);
        }
    }

    /**
     *
     * @param login
     * @param securityFilter
     * @param layer
     * @return
     * @throws ConfigurationException
     */
    private org.constellation.configuration.Layer toLayerConfig(String login, LayerSecurityFilter securityFilter, Layer layer) throws ConfigurationException {
        if (layer == null) return null;
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
            layerConfig.setTitle(layer.getTitle());
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
            return layerConfig;
        }
        return null;
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
