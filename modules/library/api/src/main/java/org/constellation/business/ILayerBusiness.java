package org.constellation.business;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Layer;
import org.constellation.dto.AddLayer;

import java.util.List;

/**
 *
 */
public interface ILayerBusiness {
    void removeAll();

    void add(AddLayer layer) throws ConfigurationException;

    void add(String name, String namespace, String providerId, String alias,
             String serviceId, String serviceType, org.constellation.configuration.Layer config) throws ConfigurationException;

    void removeForService(String serviceName, String identifier) throws ConfigurationException;

    List<Layer> getLayers(String spec, String identifier, String login) throws ConfigurationException;

    Layer getLayer(String spec, String identifier, String name, String namespace, String login) throws ConfigurationException;

    void remove(String spec, String serviceId, String layerId, String namespace) throws ConfigurationException;

}
