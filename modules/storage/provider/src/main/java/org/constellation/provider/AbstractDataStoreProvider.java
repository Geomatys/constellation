/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.geotoolkit.data.AbstractDataStoreFactory;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.memory.ExtendedDataStore;
import org.geotoolkit.data.memory.MemoryDataStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * Abstract provider which handle a Datastore.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataStoreProvider extends AbstractLayerProvider{


    private final Set<Name> index = new LinkedHashSet<Name>();
    private ExtendedDataStore store;
    private ParameterValueGroup storeConfig;

    public AbstractDataStoreProvider(final ProviderService service,
            final ParameterValueGroup config) throws DataStoreException {
        super(service,config);
        visit();
    }

    /**
     * @return the descriptor used for this datastore source configuration.
     */
    protected abstract ParameterDescriptorGroup getDatastoreDescriptor();

    /**
     * @return the datastore this provider encapsulate.
     */
    public ExtendedDataStore getDataStore(){
        return store;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return Collections.unmodifiableSet(index);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key) {
        if (!index.contains(key)) {
            key = containsOnlyLocalPart(index, key);
            if (key == null) {
                return null;
            }
        }
        final ParameterValueGroup layer = getLayer(getSource(), key.getLocalPart());
        if (layer == null) {
            return new DefaultDataStoreLayerDetails(key, store, null, null, null, null, null);

        } else {
            final List<String> styles = getLayerStyles(layer);
            return new DefaultDataStoreLayerDetails(
                    key, store, styles,
                    value(LAYER_DATE_START_FIELD_DESCRIPTOR, layer),
                    value(LAYER_DATE_END_FIELD_DESCRIPTOR, layer),
                    value(LAYER_ELEVATION_START_FIELD_DESCRIPTOR, layer),
                    value(LAYER_ELEVATION_END_FIELD_DESCRIPTOR, layer));
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
        }
    }

    @Override
    protected void visit() {
        final ParameterValueGroup source = getSource();
        storeConfig = getSourceConfiguration(source, getDatastoreDescriptor());

        final String namespace = value(AbstractDataStoreFactory.NAMESPACE, storeConfig);
        DataStore candidate = null;
        try {
            candidate = DataStoreFinder.get(storeConfig);
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }

        if (candidate == null) {
            final StringBuilder sb = new StringBuilder("Could not create datastore for parameters : \n");

            for(final GeneralParameterValue val : storeConfig.values()){
                final String key = val.getDescriptor().getName().getCode();
                final Object value;
                if(val instanceof ParameterValue){
                    value = ((ParameterValue)val).getValue();
                }else{
                    value = "~complex value~";
                }

                if (key.equals("passwd")){
                    sb.append(key).append(" : *******").append('\n');
                } else {
                    sb.append(key).append(" : ").append(value).append('\n');
                }
            }

            //use an empty datastore
            candidate = new MemoryDataStore();
        }
        store = new ExtendedDataStore(candidate);

        for(final ParameterValueGroup queryLayer : getQueryLayers(source)){
            final String layerName = value(LAYER_NAME_DESCRIPTOR, queryLayer);
            final String language = value(LAYER_QUERY_LANGUAGE, queryLayer);
            final String statement = value(LAYER_QUERY_STATEMENT, queryLayer);
            final Name name = new DefaultName(namespace, layerName);
            final Query query = QueryBuilder.language(language, statement, name);
            store.addQuery(query);
            index.add(name);
        }

        final boolean loadAll = isLoadAll(getSource());
        // if we have only queryLayer we skip this part
        if (loadAll || !getLayers(source).isEmpty()) {
            
            try {
                for (final Name name : store.getNames()) {
                    if (loadAll || containLayer(getSource(), name.getLocalPart())) {
                        index.add(name);
                    }
                }
            } catch (DataStoreException ex) {
                //Looks like we failed to retrieve the list of featuretypes,
                //the layers won't be indexed and the getCapability
                //won't be able to find thoses layers.
                getLogger().log(Level.SEVERE, "Failed to retrive list of available feature types.", ex);
            }
        }
        super.visit();
    }

}
