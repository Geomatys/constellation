/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.featurestore;

import java.util.logging.Level;

import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.ProviderType;

import static org.constellation.provider.AbstractProvider.getLogger;

import org.constellation.provider.ProviderService;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.db.postgres.PostgresFeatureStore;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FeatureStoreProvider extends AbstractDataStoreProvider{

    public FeatureStoreProvider(ProviderService service, ParameterValueGroup param) throws DataStoreException{
        super(service,param);
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

	@Override
	public ProviderType getType() {
		return ProviderType.VECTOR;
	}
    
}
