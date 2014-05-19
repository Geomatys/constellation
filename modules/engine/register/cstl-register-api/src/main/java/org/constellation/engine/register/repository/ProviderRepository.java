/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Provider;

 public interface ProviderRepository {

    List<Provider> findAll();

    Provider findOne(Integer id);

    List<Provider> findByImpl(String serviceName);

    List<String> getProviderIds();

    Provider findByIdentifie(String providerIdentifier);

    List<String> getProviderIdsForDomain(int activeDomainId);

    Provider getProviderParentIdOfLayer(String serviceType, String serviceId, String layerid);


    
}
