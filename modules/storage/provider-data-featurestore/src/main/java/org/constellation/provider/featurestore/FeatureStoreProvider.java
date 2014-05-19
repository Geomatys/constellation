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
package org.constellation.provider.featurestore;

import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractFeatureStoreProvider;
import org.constellation.provider.ProviderFactory;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.db.postgres.PostgresFeatureStore;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.util.logging.Level;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FeatureStoreProvider extends AbstractFeatureStoreProvider{

    public FeatureStoreProvider(String providerId,ProviderFactory service, ParameterValueGroup param) throws DataStoreException{
        super(providerId,service,param);
    }

    @Override
    protected FeatureStore createBaseFeatureStore() {
        //parameter is a choice of different types
        //extract the first one
        ParameterValueGroup param = getSource();
        param = param.groups("choice").get(0);
        ParameterValueGroup factoryconfig = null;
        for(GeneralParameterValue val : param.values()){
            if(val instanceof ParameterValueGroup){
                factoryconfig = (ParameterValueGroup) val;
                break;
            }
        }

        FeatureStore store = null;
        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for feature store source.");
            return null;
        }
        try {
            //create the store
            store = FeatureStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create feature store for parameters : "+factoryconfig);
            }
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return store;
    }

    /**
     * Remove all data, even postgres schema.
     */
    @Override
    public void removeAll() {
        super.removeAll();

        final FeatureStore store = createBaseFeatureStore();
        if (store instanceof PostgresFeatureStore) {
            final PostgresFeatureStore pgStore = (PostgresFeatureStore)store;
            final String dbSchema = pgStore.getDatabaseSchema();
            try {
                if (dbSchema != null && !dbSchema.isEmpty()) {
                    pgStore.dropPostgresSchema(dbSchema);
                }
            } catch (DataStoreException e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
}
