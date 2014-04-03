/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011-2014, Geomatys
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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.dao.DataRecord.DataType;

import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.DefaultName;

import org.geotoolkit.parameter.Parameters;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;
import org.geotoolkit.data.AbstractFeatureStoreFactory;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.memory.ExtendedFeatureStore;
import org.geotoolkit.data.memory.MemoryFeatureStore;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * Abstract provider which handle a Datastore.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractFeatureStoreProvider extends AbstractDataProvider{


    private final Set<Name> index = new LinkedHashSet<>();
    private ExtendedFeatureStore store;

    public AbstractFeatureStoreProvider(final String id, final ProviderFactory service,
            final ParameterValueGroup config) throws DataStoreException {
        super(id, service,config);
        visit();
    }

    protected abstract FeatureStore createBaseFeatureStore();
    
    /**
     * @return the datastore this provider encapsulate.
     */
    @Override
    public ExtendedFeatureStore getMainStore(){
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
    public Data get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data get(final Name key, Date version) {
        Name goodKey;
        if (!index.contains(key)) {
            goodKey = containsOnlyLocalPart(index, key);
            if (goodKey == null) {
                goodKey = containsWithNamespaceError(index, key);
                if (goodKey == null) {
                    return null;
                }
            }
        } else {
            goodKey = key;
        }
        final ParameterValueGroup layer = getLayer(getSource(), goodKey.getLocalPart());
        if (layer == null) {
            return new DefaultFeatureData(goodKey, store, null, null, null, null, null, version);

        } else {
            return new DefaultFeatureData(
                    goodKey, store, null,
                    value(LAYER_DATE_START_FIELD_DESCRIPTOR, layer),
                    value(LAYER_DATE_END_FIELD_DESCRIPTOR, layer),
                    value(LAYER_ELEVATION_START_FIELD_DESCRIPTOR, layer),
                    value(LAYER_ELEVATION_END_FIELD_DESCRIPTOR, layer),
                    version);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void reload() {
        dispose();
        visit();
        fireUpdateEvent();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void dispose() {
        if(store != null){
            try {
                store.close();
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        index.clear();
    }

    @Override
    protected synchronized void visit() {
        final ParameterValueGroup source = getSource();

        FeatureStore candidate = createBaseFeatureStore();
        if (candidate == null) {
            //final StringBuilder sb = new StringBuilder("Could not create featurestore : "+this.getClass().getSimpleName()+" id="+getId());

            //use an empty datastore
            candidate = new MemoryFeatureStore();
        }

        String namespace;
        final ParameterValueGroup config = candidate.getConfiguration();
        if (config != null) {
            namespace = Parameters.value(AbstractFeatureStoreFactory.NAMESPACE, config);
        } else {
            namespace = null;
        }

        if ("no namespace".equalsIgnoreCase(namespace)) {
            namespace = null;
        }
        store = new ExtendedFeatureStore(candidate);

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
                    if ((loadAll || containLayer(getSource(), name.getLocalPart())) && !index.contains(name)) {
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

    @Override
    public void remove(Name key) {
        if (store == null) {
            reload();
        }

        try {
            store.deleteFeatureType(key);
            reload();
        } catch (DataStoreException ex) {
            getLogger().log(Level.INFO, "Unable to remove "+ key.toString() +" from provider.", ex);
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.VECTOR;
    }


}