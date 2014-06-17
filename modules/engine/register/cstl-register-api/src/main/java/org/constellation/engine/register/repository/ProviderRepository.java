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

import org.constellation.engine.register.Provider;

 public interface ProviderRepository {

    List<Provider> findAll();

    Provider findOne(Integer id);
    
    Provider findByMetadataId(String metadataId);

    List<Provider> findByImpl(String serviceName);

    List<String> getProviderIds();

    Provider findByIdentifier(String providerIdentifier);
    
    Provider findByIdentifierAndType(String providerIdentifier, String type);

    List<String> getProviderIdsForDomain(int activeDomainId);

    Provider getProviderParentIdOfLayer(String serviceType, String serviceId, String layerid);

    Provider insert(Provider newProvider);

    int delete(int id);

    int deleteByIdentifier(String providerID);

    List<Provider> findChildren(String id);

}
