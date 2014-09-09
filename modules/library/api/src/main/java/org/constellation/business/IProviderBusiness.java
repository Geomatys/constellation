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

import org.constellation.api.ProviderType;
import org.constellation.engine.register.Provider;
import org.opengis.parameter.GeneralParameterValue;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface IProviderBusiness {
    List<String> getProviderIds();

    List<org.constellation.engine.register.Provider> getProviders();

    org.constellation.engine.register.Provider getProvider(int providerId);

    org.constellation.engine.register.Provider getProvider(String providerId);

    Provider getProvider(String providerId, int domainId);

    org.constellation.engine.register.Provider createProvider(String providerId, String o, ProviderType type, String factoryName, GeneralParameterValue config) throws IOException;

    List<org.constellation.engine.register.Data> getDatasFromProviderId(Integer id);

    List<org.constellation.engine.register.Style> getStylesFromProviderId(Integer id);

    void removeProvider(String providerId);

    void removeAll();

    void updateParent(String id, String providerId);

    List<Integer> getProviderIdsForDomain(int domainId);

    List<Provider> getProviderChildren(String id);
}
