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
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListenerAdapter;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;
import org.geotoolkit.process.coverage.statistics.Statistics;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.parameter.Parameters.value;
import static org.geotoolkit.process.coverage.statistics.StatisticsDescriptor.OUTCOVERAGE;

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

    public static final String STATE_PENDING    = "PENDING";
    public static final String STATE_ERROR      = "ERROR";
    public static final String STATE_COMPLETED  = "COMPLETED";
    public static final String STATE_PARTIAL    = "PARTIAL";

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

        Data data = dataRepository.findById(dataId);
        if (data == null) {
            LOGGER.log(Level.WARNING, "Can't compute coverage statistics on data id " + dataId +
                    " because data is not found in database.");
            return null;
        }
        try {
            if (DataType.COVERAGE.name().equals(data.getType())
                    && (data.isRendered() == null || !data.isRendered())
                    && data.getStatsState() == null) {
                LOGGER.log(Level.INFO, "Start computing data " + dataId + " "+data.getName()+" coverage statistics.");

                data.setStatsState(STATE_PENDING);
                updateData(data);

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

                    final org.geotoolkit.process.Process process = new Statistics(covRef, false);
                    process.addListener(new DataStatisticsListener(dataId));
                    final ParameterValueGroup out = process.call();

                    return new AsyncResult<>(value(OUTCOVERAGE, out));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during coverage statistic update for data " + dataId + " "+data.getName() + " : " + e.getMessage(), e);

            //update data
            Data lastData = dataRepository.findById(dataId);
            if (!lastData.getStatsState().equals(STATE_ERROR)) {
                data.setStatsState(STATE_ERROR);
                //data.setStatsResult(Exceptions.formatStackTrace(e));
                updateData(data);
            }
        }
        return null;
    }

    private void updateData(final Data data) {
        SpringHelper.executeInTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                dataRepository.update(data);
            }
        });
    }

    /**
     * ProcessListener that will update data record in database.
     */
    private class DataStatisticsListener extends ProcessListenerAdapter {

        private int dataId;

        public DataStatisticsListener(int dataId) {
            this.dataId = dataId;
        }

        @Override
        public void progressing(ProcessEvent event) {
            if (event.getOutput() != null) {
                final Data data = getData();
                try {
                    data.setStatsState(STATE_PARTIAL);
                    data.setStatsResult(statisticsAsString(event));
                    updateData(data);
                } catch (JsonProcessingException e) {
                    data.setStatsState(STATE_ERROR);
                    data.setStatsResult("Error during statistic serializing.");
                    updateData(data);
                }
            }
        }

        @Override
        public void completed(ProcessEvent event) {
            final Data data = getData();
            try {
                data.setStatsState(STATE_COMPLETED);
                data.setStatsResult(statisticsAsString(event));
                updateData(data);
                LOGGER.log(Level.INFO, "Data " + dataId + " " + data.getName() + " coverage statistics completed.");
            } catch (JsonProcessingException e) {
                data.setStatsState(STATE_ERROR);
                data.setStatsResult("Error during statistic serializing.");
                updateData(data);
            }
        }

        @Override
        public void failed(ProcessEvent event) {
            final Data data = getData();
            data.setStatsState(STATE_ERROR);
            //data.setStatsResult(Exceptions.formatStackTrace(event.getException()));
            updateData(data);
            Exception exception = event.getException();
            LOGGER.log(Level.WARNING, "Error during coverage statistic update for data " + dataId +
                    " "+data.getName() + " : " + exception.getMessage(), exception);

        }

        private Data getData() {
            return dataRepository.findById(dataId);
        }

        /**
         * Serialize Statistic in JSON
         * @param event
         * @return JSON String or null if event output is null.
         * @throws JsonProcessingException
         */
        private String statisticsAsString(ProcessEvent event) throws JsonProcessingException {
            final ParameterValueGroup out = event.getOutput();
            if (out != null) {
                final ImageStatistics statistics = value(OUTCOVERAGE, out);

                final ObjectMapper mapper = new ObjectMapper();
                final SimpleModule module = new SimpleModule();
                module.addSerializer(ImageStatistics.class, new ImageStatisticSerializer()); //custom serializer
                mapper.registerModule(module);
                //mapper.enable(SerializationFeature.INDENT_OUTPUT); //json pretty print
                return mapper.writeValueAsString(statistics);
            }
            return null;
        }
    }
}
