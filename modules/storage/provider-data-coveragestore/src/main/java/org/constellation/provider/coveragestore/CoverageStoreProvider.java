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
package org.constellation.provider.coveragestore;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.provider.AbstractDataProvider;
import org.constellation.provider.Data;
import org.constellation.provider.DefaultCoverageData;
import org.constellation.provider.ProviderFactory;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.postgresql.PGCoverageStore;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.observation.file.FileObservationStore;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.storage.DataFileStore;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.geotoolkit.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageStoreProvider extends AbstractDataProvider{

    private CoverageStore store;
    private Set<Name> names;

    public CoverageStoreProvider(String providerId,ProviderFactory service, ParameterValueGroup param){
        super(providerId,service,param);
        visit();
    }

    @Override
    public DataStore getMainStore() {
        return store;
    }
    
    @Override
    public synchronized void reload() {
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
            getLogger().log(Level.WARNING, "No configuration for coverage store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            store = CoverageStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create coverage store for parameters : "+factoryconfig);
            }
            names = store.getNames();
        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

        visit();
        fireUpdateEvent();
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if(store != null){
            try {
                store.close();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            store = null;
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

    public CoverageStore getStore() {
        if (store == null) {
            reload();
        }
        return store;
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
        try {
            if (store != null) {
                CoverageReference coverageReference = null;
                if ( store.handleVersioning()) {
                    VersionControl control = store.getVersioning(key);
                    if (control.isVersioned() && version != null) {
                        coverageReference = store.getCoverageReference(key, control.getVersion(version));
                    }
                }
                if(coverageReference == null) {
                    coverageReference = store.getCoverageReference(key);
                }
                return new DefaultCoverageData(key, coverageReference);
            }
        } catch (DataStoreException | VersioningException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void remove(Name key) {
        if (store == null) {
            reload();
        }

        try {
            store.delete(key);
            reload();
       } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    @Override
    public void removeAll() {
        try {
            for (Name name : names) {
                store.delete(name);
            }
            reload();

            if (store instanceof PGCoverageStore) {
                final PGCoverageStore pgStore = (PGCoverageStore)store;
                final String dbSchema = pgStore.getDatabaseSchema();
                if (dbSchema != null && !dbSchema.isEmpty()) {
                    pgStore.dropPostgresSchema(dbSchema);
                }
            }
        } catch (DataStoreException e) {
            getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.COVERAGE;
    }

    @Override
    public boolean isSensorAffectable() {
        if (store == null) {
            reload();
        }
        if (store instanceof DataFileStore) {
            try {
                final DataFileStore dfStore = (DataFileStore) store;
                final File[] files          =  dfStore.getDataFiles();
                if (files.length > 0) {
                    boolean isNetCDF = true;
                    for (File f : dfStore.getDataFiles()) {
                        if (!f.getName().endsWith(".nc")) {
                            isNetCDF = false;
                        }
                    }
                    return isNetCDF;
                }
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, "Error while retrieving file from datastore:" + getId(), ex);
            }
        }
        return super.isSensorAffectable(); 
    }
    
    public ObservationStore getObservationStore() {
        if (isSensorAffectable()) {
            if (store instanceof DataFileStore) {
                try {
                    final DataFileStore dfStore = (DataFileStore) store;
                    final File[] files = dfStore.getDataFiles();
                    //for now handle only one file
                    if (files.length > 0) {
                        final File f = files[0];
                        return new FileObservationStore(f);
                    }
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.WARNING, "Error while retrieving file from datastore:" + getId(), ex);
                }
            }
        }
        return null;
    }
}
