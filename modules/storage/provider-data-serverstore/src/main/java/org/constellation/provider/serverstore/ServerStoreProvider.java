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
package org.constellation.provider.serverstore;

import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractDataProvider;
import org.constellation.provider.DefaultCoverageData;
import org.constellation.provider.DefaultFeatureData;
import org.constellation.provider.Data;
import org.constellation.provider.ProviderFactory;
import org.geotoolkit.client.Client;
import org.geotoolkit.client.ClientFinder;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStore;
import org.constellation.admin.dao.DataRecord.DataType;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ServerStoreProvider extends AbstractDataProvider{

    private Client server;
    private Set<Name> names;

    public ServerStoreProvider(String providerId,ProviderFactory service, ParameterValueGroup param){
        super(providerId,service,param);
    }

    @Override
    public DataStore getMainStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized void reload() {
        super.reload();
        dispose();

        //pameter is a choice of different types
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

        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for server store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            server = ClientFinder.open(factoryconfig);
            if(server == null){
                throw new DataStoreException("Could not create server store for parameters : "+factoryconfig);
            }

            if(server instanceof FeatureStore){
                names = ((FeatureStore)server).getNames();
            }else if(server instanceof CoverageStore){
                names = ((CoverageStore)server).getNames();
            }else{
                names = Collections.EMPTY_SET;
            }

        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if(server != null){
            server = null;
            names = null;
        }
    }

    @Override
    public Set<Name> getKeys() {
        if(names == null){
            reload();
        }
        return names;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data get(Name key, Date version) {
        key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }

        if(server instanceof FeatureStore){
            final FeatureStore store = (FeatureStore) server;
            return new DefaultFeatureData(key, store, null, version);
        }else if(server instanceof CoverageStore){

            final CoverageStore store = (CoverageStore) server;
            try {
                if (store != null) {
                    CoverageReference coverageReference = null;
                    if (store.handleVersioning()) {
                        VersionControl control = store.getVersioning(key);
                        if (control.isVersioned() && version != null) {
                            coverageReference = store.getCoverageReference(key,control.getVersion(version));
                        }
                    }
                    if (coverageReference == null) {
                        coverageReference = store.getCoverageReference(key);
                    }
                    return new DefaultCoverageData(key, coverageReference);
                }
            } catch (DataStoreException ex) {
                getLogger().log(Level.WARNING, ex.getMessage(), ex);
            } catch (VersioningException ex) {
                getLogger().log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        return null;
    }

    @Override
    public DataType getDataType() {
        if (server instanceof FeatureStore) {
            return DataType.VECTOR;
        } else if (server instanceof CoverageStore) {
            return DataType.COVERAGE;
        } else {
            return null;
        }
    }
}
