/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
import org.constellation.provider.DefaultObservationData;
import org.constellation.provider.ProviderFactory;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.observation.ObservationStoreFinder;
import org.geotoolkit.parameter.ParametersExt;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ObservationStoreProvider extends AbstractDataProvider {

    private ObservationStore store;
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
        return DataRecord.DataType.SENSOR;
    }

    @Override
    public DataStore getMainStore() {
        if (store == null) {
            reload();
        }
        return store;
    }

    @Override
    public Data get(final Name key, final Date version) {
        if(!contains(key)){
            return null;
        }
        
        if (store != null) {

            return new DefaultObservationData(key);
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
            //create the store
            store = ObservationStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create observation store for parameters : "+factoryconfig);
            }
            names = store.getProcedureNames();
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
        if (store != null) {
            try {
                store.close();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            store = null;
            names = null;
        }
    }
    
}
