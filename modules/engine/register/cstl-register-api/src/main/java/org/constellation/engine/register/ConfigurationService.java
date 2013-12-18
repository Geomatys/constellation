package org.constellation.engine.register;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.DataBrief;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

public interface ConfigurationService {

    Object getConfiguration(String serviceType, String serviceID, String fileName, MarshallerPool pool) throws JAXBException, FileNotFoundException ;

    boolean deleteService(String identifier, String name);

    DataBrief getData(QName name, String providerId);

    String getProperty(String key, String defaultValue);

    DataBrief getDataLayer(String layerAlias, String providerId);

    ParameterValueGroup getProviderConfiguration(String serviceName, ParameterDescriptorGroup desc);

    boolean isServiceConfigurationExist(String serviceType, String identifier);

    List<String> getServiceIdentifiersByServiceType(String name);

}
