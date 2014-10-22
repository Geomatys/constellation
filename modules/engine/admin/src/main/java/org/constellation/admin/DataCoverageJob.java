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
package org.constellation.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.util.ImageStatisticSerializer;
import org.constellation.api.DataType;
import org.constellation.business.IDataCoverageJob;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;
import org.geotoolkit.process.coverage.statistics.Statistics;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
@Component
@Primary
public class DataCoverageJob implements IDataCoverageJob {

    /**
     * Used for debugging purposes.
     */
    protected static final Logger LOGGER = Logging.getLogger(DataCoverageJob.class);

    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;

    /**
     * Injected data repository.
     */
    @Inject
    private ProviderRepository providerRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public Future<ImageStatistics> asyncUpdateDataStatistics(final int dataId) {

        try {
            Data data = dataRepository.findById(dataId);

            if (data != null && DataType.COVERAGE.name().equals(data.getType())) {
                LOGGER.log(Level.INFO, "Start computing data " + dataId + " coverage statistics.");

                data.setStats("PENDING");
                dataRepository.update(data);

                final Provider provider = providerRepository.findOne(data.getProvider());
                final DataProvider dataProvider = DataProviders.getInstance().getProvider(provider.getIdentifier());
                final DataStore store = dataProvider.getMainStore();
                if (store instanceof CoverageStore) {
                    final CoverageStore coverageStore = (CoverageStore) store;

                    Name name = new DefaultName(data.getNamespace(), data.getName());
                    if (data.getNamespace() == null || data.getNamespace().isEmpty()) {
                        name = new DefaultName(data.getName());
                    }
                    final CoverageReference covRef = coverageStore.getCoverageReference(name);

                    final ImageStatistics analyse = Statistics.analyse(covRef, false);
                    final ObjectMapper mapper = new ObjectMapper();
                    final SimpleModule module = new SimpleModule();
                    module.addSerializer(ImageStatistics.class, new ImageStatisticSerializer()); //custom serializer
                    mapper.registerModule(module);
                    mapper.enable(SerializationFeature.INDENT_OUTPUT); //json pretty print

                    //get last version of data
                    data = dataRepository.findById(dataId);
                    data.setStats(mapper.writeValueAsString(analyse));
                    dataRepository.update(data);
                    return new AsyncResult<>(analyse);
                }
            }
        } catch (DataStoreException | ProcessException | JsonProcessingException e) {
            LOGGER.log(Level.WARNING, "Error during coverage statistic update for data "+dataId+" : "+e.getMessage(), e);
        }
        return null;
    }
}
