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
package org.constellation.provider.observationstore;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.dao.DataRecord;
import org.constellation.provider.AbstractDataProvider;
import org.constellation.provider.Data;
import org.constellation.provider.DefaultFeatureData;
import org.constellation.provider.ProviderFactory;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.observation.ObservationStoreFinder;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ObservationStoreProvider extends AbstractDataProvider {

    private FeatureStore featureStore;
    private ObservationStore observationStore;
    private Set<Name> names;
    
    public ObservationStoreProvider(final String providerId, final ProviderFactory service, final ParameterValueGroup param){
        super(providerId,service,param);
        visit();
    }
    
    @Override
    public Set<Name> getKeys() {
        if(names == null){
            reload();
        }
        return names;
    }

    @Override
    public Data get(final Name key) {
        return get(key, null);
    }

    @Override
    public DataRecord.DataType getDataType() {
        return DataRecord.DataType.VECTOR;
    }

    @Override
    public DataStore getMainStore() {
        if (featureStore == null) {
            reload();
        }
        return featureStore;
    }

    public ObservationStore getObservationStore() {
        if (observationStore == null) {
            reload();
        }
        return observationStore;
    }

    @Override
    public Data get(final Name key, final Date version) {
        if(!contains(key)){
            return null;
        }
        final Name goodKey = fullyQualified(key);
        
        if (featureStore != null) {
            return new DefaultFeatureData(goodKey, featureStore, null, null, null, null, null, version);
        }
        return null;
    }

    @Override
    public void reload() {
        dispose();

        //parameter is a choice of different types
        //extract the first one
        ParameterValueGroup param = getSource();
        param = ParametersExt.getOrCreateGroup(param, "choice");
        ParameterValueGroup factoryconfig = null;
        for(GeneralParameterValue val : param.values()){
            if(val instanceof ParameterValueGroup){
                factoryconfig = (ParameterValueGroup) val;
                break;
            }
        }

        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for observation store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the observation store
            observationStore = ObservationStoreFinder.open(factoryconfig);
            if(observationStore == null){
                throw new DataStoreException("Could not create observation store for parameters : " + factoryconfig);
            }
            
            //create the feature store
            featureStore = FeatureStoreFinder.open(factoryconfig);
            if(featureStore == null){
                throw new DataStoreException("Could not create feature store for parameters : " + factoryconfig);
            }
            names = featureStore.getNames();
        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

        visit();
        fireUpdateEvent();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (observationStore != null) {
            try {
                observationStore.close();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            observationStore = null;
            names = null;
        }
    }

    @Override
    public boolean isSensorAffectable() {
        return true;
    }
}
