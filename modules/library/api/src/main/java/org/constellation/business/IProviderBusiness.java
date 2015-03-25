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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.api.ProviderType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.dto.ProviderPyramidChoiceList;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Style;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.storage.DataStoreFactory;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IProviderBusiness {
    List<String> getProviderIds();

    List<Provider> getProviders();

    Provider getProvider(int providerId);

    Provider getProvider(String providerId);

    Provider getProvider(String providerId, int domainId);

    /**
     * Create and save a provider object from input identifier and {@link org.constellation.configuration.ProviderConfiguration} object.
     *
     * @param domainId identifier of the domain to put created provider into.
     * @param id The identifier (name) to give to the created provider.
     * @param config Serialized provider configuration (entire parameter group, as defined in the matching {@link org.constellation.provider.DataProviderFactory}.
     * @return A new {@link Provider} object, containing given configuration.
     * @throws ConfigurationException If a provider already exists with the given name, or if the configuration is invalid.
     *
     * @deprecated : Following procedure will be removed once the new DataStoreSource system will be created.
     */
    Provider create(int domainId, String id, ProviderConfiguration config) throws ConfigurationException;

    /**
     * Create and save a provider object with given identifier. Input spi and configuration must be {@link org.geotoolkit.storage.DataStoreFactory}
     * and its proper configuration filled from {@link org.geotoolkit.storage.DataStoreFactory#getParametersDescriptor()}.
     *
     * @param domainId identifier of the domain to put created provider into.
     * @param id The identifier (name) to give to the created provider.
     * @param spi {@link org.geotoolkit.storage.DataStoreFactory} to identify underlying data source type.
     * @param spiConfiguration The configuration needed for spi parameter to open a valid data source.
     * @return A new {@link Provider} object, containing given configuration.
     * @throws ConfigurationException If a provider already exists with the given name, or if the configuration is invalid.
     *
     * @deprecated : Following procedure will be removed once the new DataStoreSource system will be created.
     */
    Provider create(final int domainId, final String id, final DataStoreFactory spi, ParameterValueGroup spiConfiguration) throws ConfigurationException;

    /**
     * Create and save a provider object with given identifier. Input spi and configuration must be {@link org.constellation.provider.DataProviderFactory}
     * and its proper configuration filled from {@link org.constellation.provider.DataProviderFactory#getProviderDescriptor()}.
     *
     * @param domainId identifier of the domain to put created provider into.
     * @param id The identifier (name) to give to the created provider.
     * @param providerSPIName Name of the {@link org.constellation.provider.DataProviderFactory} to identify underlying data source type.
     * @param providerConfig The configuration needed for providerSPI parameter to open a valid data source.
     * @return A new {@link Provider} object, containing given configuration.
     * @throws ConfigurationException If a provider already exists with the given name, or if the configuration is invalid.
     *
     * @deprecated : Following procedure will be removed once the new DataStoreSource system will be created.
     */
    Provider create(final int domainId, final String id, final String providerSPIName, final ParameterValueGroup providerConfig) throws ConfigurationException;

    Set<Name> test(String providerIdentifier, ProviderConfiguration configuration) throws DataStoreException, ConfigurationException;

    void update(int domainId, String id, ProviderConfiguration config) throws ConfigurationException;

    Provider storeProvider(String providerId, String o, ProviderType type, String factoryName, GeneralParameterValue config) throws IOException;

    List<Data> getDatasFromProviderId(Integer id);

    List<Style> getStylesFromProviderId(Integer id);

    void removeProvider(String providerId);

    void removeAll();

    void updateParent(String id, String providerId);

    List<Integer> getProviderIdsForDomain(int domainId);

    List<Provider> getProviderChildren(String id);

    ProviderPyramidChoiceList listPyramids(final String id, final String layerName) throws DataStoreException;

    /**
     * Generates a pyramid conform for data.
     * N.B : Generated pyramid contains coverage real values, it's not styled for rendering.
     *
     * @param providerId Provider identifier of the data to tile.
     * @param dataName the given data name.
     * @return {@link DataBrief}
     */
    DataBrief createPyramidConform(final String providerId,final String dataName, final String namespace,final int userId) throws ConstellationException;

}
