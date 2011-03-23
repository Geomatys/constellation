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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.data.AbstractDataStoreFactory;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.memory.ExtendedDataStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.storage.DataStoreException;
import org.opengis.feature.type.Name;

/**
 * Abstract provider which handle a Datastore.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataStoreProvider extends AbstractLayerProvider{


    private final Map<String,Serializable> params;
    private final Set<Name> index = new LinkedHashSet<Name>();
    private final ExtendedDataStore store;

    public AbstractDataStoreProvider(final ProviderService service,
            final ProviderSource config) throws DataStoreException {
        super(service,config);

        params = prepareParameters(config.parameters);

        final String namespace = (String) params.get(AbstractDataStoreFactory.NAMESPACE.getName().getCode());

        final DataStore candidate = DataStoreFinder.getDataStore(params);

        if (candidate == null) {
            final StringBuilder sb = new StringBuilder("Could not create datastore for parameters : \n");
            for (final Map.Entry<String,Serializable> entry : params.entrySet()){
                if (entry.getKey().equals("passwd")){
                    sb.append(entry.getKey()).append(" : *******").append('\n');
                } else {
                    sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
                }
            }
            throw new DataStoreException(sb.toString());
        }


        store = new ExtendedDataStore(candidate);

        for(final ProviderLayer layer : config.querylayers){
            final Query query = QueryBuilder.language(layer.language, layer.statement);
            final Name name = new DefaultName(namespace, layer.name);
            store.addQuery(query, name);
        }

        visit();
    }

    /**
     * @return the datastore this provider encapsulate.
     */
    public ExtendedDataStore getDataStore(){
        return store;
    }

    public abstract Map<String,Serializable> prepareParameters(Map<String,String> params);

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
        final ProviderLayer layer = source.getLayer(key.getLocalPart());
        if (layer == null) {
            return new DefaultDataStoreLayerDetails(key, store, null, null, null, null, null);

        } else {
            final List<String> styles = layer.styles;
            return new DefaultDataStoreLayerDetails(key, store, styles,
                    layer.dateStartField, layer.dateEndField,
                    layer.elevationStartField, layer.elevationEndField);
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
            params.clear();
            source.layers.clear();
            source.parameters.clear();
        }
    }

    @Override
    protected void visit() {
        try {
            for (final Name name : store.getNames()) {
                if (source.loadAll || source.containsLayer(name.getLocalPart())) {
                    index.add(name);
                }
            }
        } catch (DataStoreException ex) {
            //Looks like we failed to retrieve the list of featuretypes,
            //the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            getLogger().log(Level.SEVERE, "Failed to retrive list of available feature types.", ex);
        }
        super.visit();
    }

}
