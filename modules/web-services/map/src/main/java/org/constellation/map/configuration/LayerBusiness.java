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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.AddLayer;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.constellation.utils.CstlMetadatas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class LayerBusiness {
    
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
        
        final Service service = serviceRepository.findByIdentifierAndType(addLayerData.getServiceType(), addLayerData.getServiceId());
        
        if (service !=null) {
            final String name       = addLayerData.getLayerId();
            final String providerId = addLayerData.getProviderId();
            final String alias      = addLayerData.getLayerAlias();
            
            // look for layer namespace
            final String namespace;
            if (addLayerData.getLayerNamespace() != null) {
                namespace = addLayerData.getLayerNamespace();
            } else {
                final DataProvider provider = DataProviders.getInstance().getProvider(addLayerData.getProviderId());
                namespace = ProviderParameters.getNamespace(provider);
            }

            final Data data = dataRepository.findDataFromProvider(namespace, name, providerId);
            Layer layer = new Layer(name, namespace, alias, service.getId(), data.getId(), System.currentTimeMillis(), -1, -1, null, securityManager.getCurrentUserLogin());
            layer = layerRepository.save(layer);
            
            //style
            for (int styleID : data.getStyles()) {
                styleRepository.linkStyleToLayer(styleID, layer.getId());
            }
            
            //update service ISO metadata
            if (service.hasIsoMetadata()) {
                try {
                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(service.getMetadata());
                    CstlMetadatas.addServiceMetadataLayer(servMeta, name);
                    final String srm = MetadataIOUtils.marshallMetadataToString(servMeta);
                    serviceRepository.updateIsoMetadata(service, servMeta.getFileIdentifier(), srm);
                } catch (JAXBException e) {
                    throw new ConstellationPersistenceException(e);
                }
            }
            
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + addLayerData.getServiceId());
        }
    }
    
    public void remove(final String spec, final String serviceId, final QName layerId) throws ConfigurationException {
        final Service service = serviceRepository.findByIdentifierAndType(serviceId, spec);
        if (service != null) {
            final Layer layer = layerRepository.findByServiceIdAndLayerName(service.getId(), layerId.getLocalPart(), layerId.getNamespaceURI());
            if (layer != null) {
                layerRepository.delete(layer.getId());
            } else {
                throw new TargetNotFoundException("Unable to find a layer: " + layerId);
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + serviceId);
        }
    }
    
    public List<org.constellation.configuration.Layer> getLayers(final String spec, final String identifier) throws ConfigurationException {
        final List<org.constellation.configuration.Layer> response = new ArrayList<>();
        final Service service = serviceRepository.findByIdentifierAndType(identifier, spec);
        if (service != null) {
            final List<Layer> layers = layerRepository.findByServiceId(service.getId());
            for (Layer layer : layers) {
                final Data data          = dataRepository.findById(layer.getData());
                final Provider provider  = providerRepository.findOne(data.getProvider());
                final QName name         = new QName(layer.getNamespace(), layer.getName());
                final List<Style> styles = styleRepository.findByLayer(layer);
                
                final org.constellation.configuration.Layer layerDto = new org.constellation.configuration.Layer(name);
                layerDto.setAlias(layer.getAlias());
                // TODO layerDto.setAbstrac();
                // TODO layerDto.setAttribution(null);
                // TODO layerDto.setAuthorityURL(null);
                // TODO layerDto.setCrs(null);
                // TODO layerDto.setDataURL(null);
                layerDto.setDate(new Date(layer.getDate()));
                // TODO layerDto.setDimensions(null);
                // TODO layerDto.setFilter(null);
                // TODO layerDto.setGetFeatureInfoCfgs(null);
                // TODO layerDto.setKeywords();
                // TODO layerDto.setMetadataURL(null);
                // TODO layerDto.setOpaque(Boolean.TRUE);
                layerDto.setOwner(layer.getOwner());
                layerDto.setProviderID(provider.getIdentifier());
                layerDto.setProviderType(provider.getType());
                
                for (Style style : styles) {
                    final Provider styleProvider = providerRepository.findOne(style.getProvider());
                    DataReference styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, styleProvider.getIdentifier(), style.getName());
                    layerDto.getStyles().add(styleProviderReference);
                }
                
                 // TODO layerDto.setTitle(null);
                 // TODO layerDto.setVersion();
                response.add(layerDto);
            }
        } else {
            throw new TargetNotFoundException("Unable to find a service:" + identifier);
        }
        return response;
    }
}
