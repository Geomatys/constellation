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

import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.DefaultCoverageStoreLayerDetails;
import org.constellation.provider.DefaultDataStoreLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
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
public class ServerStoreProvider extends AbstractLayerProvider{

    private Client server;
    private Set<Name> names;

    public ServerStoreProvider(ProviderService service, ParameterValueGroup param){
        super(service,param);
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
    public LayerDetails get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key, Date version) {
        key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }

        if(server instanceof FeatureStore){
            final FeatureStore store = (FeatureStore) server;
            return new DefaultDataStoreLayerDetails(key, store, null, version);
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
                    return new DefaultCoverageStoreLayerDetails(key, coverageReference);
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
