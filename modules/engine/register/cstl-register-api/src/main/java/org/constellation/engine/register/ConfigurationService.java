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
package org.constellation.engine.register;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.DataBrief;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Service;

public interface ConfigurationService {

//    void storeConfiguration(String serviceType, String serviceID, String fileName, Object obj, MarshallerPool pool, String login);

    Object getConfiguration(String serviceType, String serviceID, String fileName, MarshallerPool pool) throws JAXBException, FileNotFoundException ;

    boolean deleteService(String identifier, String name);

    DataBrief getData(QName name, String providerId);

    String getProperty(String key, String defaultValue);

    DataBrief getDataLayer(String layerAlias, String providerId);

    boolean isServiceConfigurationExist(String serviceType, String identifier);

    List<String> getServiceIdentifiersByServiceType(String name);

    Service readServiceMetadata(String identifier, String serviceType, String language) throws JAXBException, IOException;

    List<String> getProviderIdentifiers();

    void deleteData(String namespaceURI, String localPart, String providerIdentifier);

    void deleteProvider(String providerID);

    List<Provider> findProvidersByImpl(String serviceName);

    Service findServiceByIdentifierAndType(String serviceID, String serviceType);


}
