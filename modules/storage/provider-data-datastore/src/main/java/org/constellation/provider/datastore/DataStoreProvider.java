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
package org.constellation.provider.datastore;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.DefaultDataStoreLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DataStoreProvider extends AbstractLayerProvider{

    private DataStore store;
    private Set<Name> names;

    public DataStoreProvider(ProviderService service, ParameterValueGroup param){
        super(service,param);
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
            getLogger().log(Level.WARNING, "No configuration for data store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            store = DataStoreFinder.get(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create data store for parameters : "+factoryconfig);
            }
            names = store.getNames();
        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if(store != null){
            store.dispose();
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
    
    @Override
    public LayerDetails get(Name key) {
        key = fullyQualified(key);        
        if(!contains(key)){
            return null;
        }
        return new DefaultDataStoreLayerDetails(key, store, null);
    }

}
