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

import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.Session;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.ProviderService;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.db.postgres.PostgresFeatureStore;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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
     * {@inheritDoc}
     */
    @Override
    protected synchronized void visit() {
        super.visit();

        // Update administration database.
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            ProviderRecord pr = session.readProvider(this.getId());
            if (pr == null) {
                pr = session.writeProvider(this.getId(), ProviderType.LAYER, "feature-store", getSource(), null);
            }
            final List<DataRecord> list = pr.getData();
            final Set<Name> names = this.getDataStore().getNames();

            // Remove no longer existing data.
            for (final DataRecord data : list) {
                boolean found = false;
                for (final Name key : names) {
                    if (data.getName().equals(key.getLocalPart())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    session.deleteData(this.getId(), data.getName());
                }
            }

            // Add not registered new data.
            for (final Name key : names) {
                boolean found = false;
                for (final DataRecord data : list) {
                    if (key.getLocalPart().equals(data.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    session.writeData(key.getLocalPart(), pr, DataType.VECTOR, null);
                }
            }
        } catch (IOException | SQLException ex) {
            getLogger().log(Level.WARNING, "An error occurred while updating database on provider startup.", ex);
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, "An error occurred wile scanning provider FeatureStore entries.", ex);
        } finally {
            if (session != null) session.close();
        }
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
