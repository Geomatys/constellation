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

import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Style;

public interface ProviderRepository {

    public List<Provider> findAll();

    public Provider findOne(Integer id);

    public List<Provider> findByImpl(String serviceName);

    public List<String> getProviderIds();

    public Provider findByIdentifier(String providerIdentifier);

    public Provider findByIdentifierAndType(String providerIdentifier, String type);

    public List<Integer> getProviderIdsForDomain(int activeDomainId);

    public Provider getProviderParentIdOfLayer(String serviceType, String serviceId, String layerid);

    public Provider insert(Provider newProvider);

    public int delete(int id);

    public int deleteByIdentifier(String providerID);

    public List<Provider> findChildren(String id);

    public List<Data> findDatasByProviderId(Integer id);

    public int update(Provider provider);

    public List<Style> findStylesByProviderId(Integer providerId);

    public Provider findByIdentifierAndDomainId(String providerIdentifier, Integer domainId);
}
