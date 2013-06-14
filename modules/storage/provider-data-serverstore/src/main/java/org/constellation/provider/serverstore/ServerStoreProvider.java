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
package org.constellation.provider.serverstore;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.*;
import org.geotoolkit.client.Server;
import org.geotoolkit.client.ServerFinder;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.data.FeatureStore;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ServerStoreProvider extends AbstractLayerProvider{

    private Server server;
    private Set<Name> names;

    public ServerStoreProvider(ProviderService service, ParameterValueGroup param){
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
            getLogger().log(Level.WARNING, "No configuration for server store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            server = ServerFinder.open(factoryconfig);
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

    @Override
    public LayerDetails get(Name key) {
        key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }

        if(server instanceof FeatureStore){
            final FeatureStore store = (FeatureStore) server;
            return new DefaultDataStoreLayerDetails(key, store, null);
        }else if(server instanceof CoverageStore){
            final CoverageStore store = (CoverageStore) server;
            try {
                return new DefaultCoverageStoreLayerDetails(key, store.getCoverageReference(key));
            } catch (DataStoreException ex) {
                getLogger().log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        return null;
    }

}
