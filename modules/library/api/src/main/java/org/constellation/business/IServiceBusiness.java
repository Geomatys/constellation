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
package org.constellation.business;

import java.util.List;
import java.util.Map;

import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.Details;
import org.constellation.engine.register.jooq.tables.pojos.Service;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IServiceBusiness {
    void stop(String service, String identifier) throws ConfigurationException;

    void configure(String serviceType, String identifier, Details serviceMetadata, Object configuration) throws ConfigurationException;

    void start(String serviceType) throws ConfigurationException;

    void start(String serviceType, String identifier) throws ConfigurationException;

    void restart(String serviceType, String identifier, boolean closeFirst) throws ConfigurationException;

    void rename(String serviceType, String identifier, String newIdentifier) throws ConfigurationException;

    void delete(String serviceType, String identifier) throws ConfigurationException;

    Service ensureExistingInstance(String spec, String identifier) throws ConfigurationException;

    Object getConfiguration(String serviceType, String identifier) throws ConfigurationException;

    /**
     * Create a new service instance from input information.
     * @param serviceType Type of service to instantiate (CSW, WMS, etc.)
     * @param identifier The name to give to the service.
     * @param configuration An optional configuration specific to the queried type of service (WPS --> PRocessContext, etc.).
     * @param serviceMetadata An ISO 19115-2 metadata file to describe the service. If null a default empty metadata will be created.
     * @param domainId A domain to put service into. Optional.
     * @return
     * @throws ConfigurationException
     */
    Object create(String serviceType, String identifier, Object configuration, Details serviceMetadata, Integer domainId) throws ConfigurationException;

    List<String> getServiceIdentifiers(String wms);

    /**
     * Try to retrieve a service of the given type, using its name.
     * @param type Type of the service (WMTS, WPS, etc.) we search.
     * @param id The name of the service to return.
     * @return A service of the queried type matching input name, or null if we cannot find any.
     */
    Service getServiceByIdentifierAndType(String type, String id);

    Details getInstanceDetails(String service, String id, String language) throws ConfigurationException;

    Object getExtraConfiguration(String serviceType, String identifier, String fileName) throws ConfigurationException;

    Object getExtraConfiguration(String sos, String id, String s, MarshallerPool instance) throws ConfigurationException;

    void setExtraConfiguration(String serviceType, String identifier, String fileName, Object config, MarshallerPool pool);

    void deleteAll() throws ConfigurationException;

    Map<String,ServiceStatus> getStatus(String spec);

    Instance getI18nInstance(String serviceType, String identifier, String lang);

    List<ServiceDTO> getAllServicesByDomainId(int domainId, String lang) throws ConfigurationException;

    List<ServiceDTO> getAllServicesByDomainIdAndType(int domainId, String lang, String type) throws ConfigurationException;

    void addServiceToDomain(int serviceId, int domainId);

    void removeServiceFromDomain(int serviceId, int domainId);

    void setInstanceDetails(String serviceType, String identifier, Details details, String language,
                            boolean default_) throws ConfigurationException;
}
