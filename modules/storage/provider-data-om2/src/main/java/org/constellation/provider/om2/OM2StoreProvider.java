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
package org.constellation.provider.om2;

import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractFeatureStoreProvider;
import org.constellation.provider.ProviderService;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.util.logging.Level;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class OM2StoreProvider extends AbstractFeatureStoreProvider{

    public OM2StoreProvider(ProviderService service, ParameterValueGroup param) throws DataStoreException{
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
            getLogger().log(Level.WARNING, "No configuration for om2 store source.");
            return null;
        }
        try {
            //create the store
            store = FeatureStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create om2 store for parameters : "+factoryconfig);
            }
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return store;
    }

    /**
     * Remove all data.
     */
    @Override
    public void removeAll() {
        super.removeAll();
    }
    
}
