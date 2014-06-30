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
package org.constellation.engine.register.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.constellation.engine.register.*;
import org.constellation.engine.register.i18n.ServiceWithI18N;

public interface ServiceRepository {
	
    int create(Service service);
	
    List<Service> findAll();
    
    Service findById(int id);
    
    List<Service> findByDataId(int dataId);
    
    List<Service> findByDomain(int domainId);
    
    List<Service> findByType(String type);
    
    Service findByIdentifierAndType(String id, String type);

    void delete(Integer id);

    List<String> findIdentifiersByType(String type);

    ServiceMetadata findMetaDataForLang(int serviceId, String language);
    
    void writeMetadataForLang(ServiceMetadata metadata);

    List<ServiceExtraConfig> getExtraConfig(int id);
    
    ServiceExtraConfig getExtraConfig(int id, String filename);

    java.util.Map<String, Set<String>> getAccessiblesServicesByType(int domainId, String name);

    Service save(Service service);

    Service updateConfig(Service service);

    void updateExtraFile(Service service, String fileName, String config);

    int updateIsoMetadata(Service service, String fileIdentifier, String string);

    Map<Domain, Boolean> getLinkedDomains(int serviceId);

    Service findByMetadataId(String metadataId);

    List<Data> findDataByServiceId(Integer id);
    
    ServiceWithI18N i18n(Service service);
}
