/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.provider;

import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.provider.configuration.Configurator.ProviderInformation;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.util.NamesExt;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.Provider.RELOAD_TIME_PROPERTY;
import org.geotoolkit.storage.coverage.CoverageReference;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.opengis.util.GenericName;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for
 * different kind of data sources, postgrid, shapefile ...
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public final class DataProviders extends Providers implements PropertyChangeListener {

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private long lastUpdateTime = System.currentTimeMillis();
    protected final Class<GenericName> keyClass = GenericName.class;
    protected final Class<Data> valClass = Data.class;
    
    //all loaded providers
    private Collection<DataProvider> PROVIDERS = null;

    /**
     * {@inheritDoc}
     */
    public boolean contains(GenericName key) {
        return getKeys().contains(key);
    }

    /**
     * Empty implementation.
     */
    public void remove(GenericName key) {
    }

    protected synchronized void fireUpdateEvent(){
        final long oldTime = lastUpdateTime;
        lastUpdateTime = System.currentTimeMillis();
        listeners.firePropertyChange(RELOAD_TIME_PROPERTY, oldTime, lastUpdateTime);
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        //a provider has been updated
        final Object source = evt.getSource();
        if(source instanceof Provider){
            //save changed configuration
            final Provider provider = (Provider) source;
            try {
                getConfigurator().updateProviderConfiguration(provider.getId(), provider.getSource());
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        //forward events
        fireUpdateEvent();
    }

    public DataProvider createProvider(final String id,
                                       final DataProviderFactory factory,
                                       final ParameterValueGroup params,
                                       final Integer datasetID) throws ConfigurationException{
        return createProvider(id,factory,params,datasetID,true);

    }

    /**
     * Creates provider for given params
     * @param id provider id
     * @param factory given provider factory
     * @param params given parameters
     * @param datasetID given an existing datasetId to attach with data
     * @param createDatasetIfNull flag that indicates if a dataset will be created in case of given datasetID is null.
     * @return
     * @throws ConfigurationException
     */
    public DataProvider createProvider(final String id,
                                       final DataProviderFactory factory,
                                       final ParameterValueGroup params,
                                       final Integer datasetID,
                                       final boolean createDatasetIfNull) throws ConfigurationException{
        
        final DataProvider provider = factory.createProvider(id,params);

        //ensure the store is at least correct
        final DataStore ds = provider.getMainStore();
        try{
            if (ds instanceof FeatureStore) {
                ((FeatureStore) ds).getNames();
            } else if (ds instanceof CoverageStore) {
                ((CoverageStore) ds).getNames();
            }
        }catch(DataStoreException ex){
            try {
                ds.close();
            } catch (DataStoreException ex1) {
                //we have try
            }
            throw new ConfigurationException(ex.getMessage(),ex);
        }

        //add in the list our provider
        getProviders();
        provider.addPropertyListener(this);
        PROVIDERS.add(provider);
        //save the configuration
        getConfigurator().addProviderConfiguration(id,params, datasetID, createDatasetIfNull);
        fireUpdateEvent();
        return provider;
    }

    public Set<GenericName> testProvider(String id, final DataProviderFactory factory,
                                  final ParameterValueGroup params) throws DataStoreException {
        getProviders();
        final DataProvider provider = factory.createProvider(id, params);
        //test to read data
        Set<GenericName> names = new HashSet<>();
        final DataStore ds = provider.getMainStore();
        if (ds instanceof FeatureStore) {
            names = ((FeatureStore) ds).getNames();
        } else if (ds instanceof CoverageStore) {
            names = ((CoverageStore) ds).getNames();
        }
        return names;
    }

    public HashMap<GenericName, CoordinateReferenceSystem> getCRS(String id) throws DataStoreException {
        HashMap<GenericName,CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = new HashMap<>();
        getProviders();
        final DataProvider provider = getProvider(id);
        //test getting CRS from data
        final DataStore store = provider.getMainStore();
        if (store instanceof FeatureStore) {
            final FeatureStore fs = (FeatureStore) store;
            final Set<GenericName> names =  fs.getNames();
            for (final GenericName name : names){
                final FeatureType ft = fs.getFeatureType(name);
                final CoordinateReferenceSystem crs = ft.getCoordinateReferenceSystem();
                if(crs!=null) {
                    nameCoordinateReferenceSystemHashMap.put(name,crs);
                }
            }
        } else if (store instanceof CoverageStore) {
            final CoverageStore cs = (CoverageStore) store;
            final Set<GenericName> names = cs.getNames();
            for (final GenericName name : names){
                final CoverageReference coverageReference = cs.getCoverageReference(name);
                final GridCoverageReader coverageReader = coverageReference.acquireReader();
                try {
                    final CoordinateReferenceSystem crs = coverageReader.getGridGeometry(coverageReference.getImageIndex()).getCoordinateReferenceSystem();
                    if(crs!=null) {
                        nameCoordinateReferenceSystemHashMap.put(name,crs);
                    }
                }finally {
                    coverageReference.recycle(coverageReader);
                }
            }
        }
        return nameCoordinateReferenceSystemHashMap;
    }

    public DataProvider removeProvider(final DataProvider provider) throws ConfigurationException{
        if(provider==null) return null;
        getConfigurator().removeProviderConfiguration(provider.getId());
        
        final boolean b = PROVIDERS.remove(provider);
        if(b){
            provider.removePropertyListener(this);
            fireUpdateEvent();
        }

        return provider;
    }

    /**
     * {@inheritDoc }
     */
    public Set<GenericName> getKeys() {
        return getKeys(null);
    }

    /**
     * {@inheritDoc }
     */
    public Set<GenericName> getKeys(final String sourceId) {
        final Set<GenericName> keys = new HashSet<>();
        for(final DataProvider provider : getProviders()){
            keys.addAll(provider.getKeys(sourceId));
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     * @deprecated use get(key, providerID) instead because two provider can have the same named layer
     */
    @Deprecated
    public Data get(final GenericName key) {
        final List<Data> candidates = new ArrayList<>();

        for(final Provider<GenericName,Data> provider : getProviders()){
            final Data layer = provider.get(key);
            if(layer != null) candidates.add(layer);
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(Data.class.isAssignableFrom(valClass)){
                //make a more accurate search testing both namespace and local part are the same.
                final GenericName nk = (GenericName) key;
                for(int i=0;i<candidates.size();i++){
                    final Data ld = (Data) candidates.get(i);
                    if(Objects.equals(NamesExt.getNamespace(ld.getName()), NamesExt.getNamespace(nk))
                            && Objects.equals(ld.getName().tip().toString(), nk.tip().toString())) {
                        return (Data)ld;
                    }
                }

                //we could not find one more accurate then another
                return candidates.get(0);
            }else{
                return candidates.get(0);
            }
        }

        return null;
    }

    public Data get(final GenericName key, final String providerID) {
        final Provider<GenericName,Data> provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        return provider.get(key);
    }

    public List<Data> getAll() {
        final List<Data> values = new ArrayList<>();
        for(Provider<GenericName,Data> provider : getProviders()){
            for(GenericName key : provider.getKeys()){
                values.add(provider.get(key));
            }
        }
        return values;
    }

    public synchronized Collection<DataProvider> getProviders(){
        if(PROVIDERS != null){
            return Collections.unmodifiableCollection(PROVIDERS);
        }

        final List<ProviderInformation> configs;
        try {
            configs = getConfigurator().getProviderInformations();
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return Collections.EMPTY_LIST;
        }
        
        final List<DataProvider> cache = new ArrayList<>();
        
        //rebuild providers
        for(ProviderInformation entry : configs){
            final String providerId = entry.id;
            final ParameterValueGroup params = entry.config;
            final String impl = entry.impl;
                
            for(final ProviderFactory factory : getFactories()){
                //check if config can be used by this factory
                if(factory.getName().equals(impl) && factory.canProcess(params)){
                    try{
                        final DataProvider prov = (DataProvider)factory.createProvider(providerId, params);
                        if(prov != null){
                            prov.addPropertyListener(this);
                            cache.add(prov);
                        }
                    }catch(Exception ex){
                        //we must not fail here in any case
                        LOGGER.log(Level.SEVERE, "Factory "+factory.getName()+" failed to create a provider.",ex);
                    }
                }
            }
        }
        

        PROVIDERS = cache;
        fireUpdateEvent();
        return Collections.unmodifiableCollection(PROVIDERS);
    }

    /**
     * Get the provider identified by given name.
     * @param id The identifier of the data provider to find.
     * @return Provider found for requestde name, or null if we cannot find it.
     */
    public synchronized DataProvider getProvider(final String id){
        for (DataProvider provider : getProviders()) {
            if (provider.getId().equals(id)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public synchronized void reload() {
        dispose();
        getProviders(); //will load providers
        fireUpdateEvent();
    }

    /**
     * {@inheritDoc }
     */
    public synchronized void dispose() {
        if(PROVIDERS == null){
            //providers are not loaded
            return;
        }

        try{
            //sproviders were loaded, dispose each of them
            for(final Provider<GenericName,Data> provider : getProviders()){
                try{
                    provider.removePropertyListener(this);
                    provider.dispose();
                }catch(Exception ex){
                    //we must not fail here in any case
                    LOGGER.log(Level.SEVERE, "Failed to dispose provider : " + provider.toString(),ex);
                }
            }
        }finally{
            //there should not be an error, but in worse case ensure this is correctly set to null.
            PROVIDERS = null;
        }
    }
    
    private static final DataProviders INSTANCE = new DataProviders();
    //all providers factories, unmodifiable
    private static final Collection<DataProviderFactory> FACTORIES;

    static {
        final List<DataProviderFactory> cache = new ArrayList<>();
        final ServiceLoader<DataProviderFactory> loader = ServiceLoader.load(DataProviderFactory.class);
        for(final DataProviderFactory factory : loader){
            cache.add(factory);
        }
        FACTORIES = Collections.unmodifiableCollection(cache);
    }

    /**
     * {@inheritDoc }
     */
    public ElevationModel getElevationModel(final GenericName name) {
        for(final DataProvider provider : getProviders()){
            final ElevationModel model = provider.getElevationModel(name);
            if(model != null) return model;
        }
        return null;
    }

    public Data get(final GenericName key, final Date version) {
        final List<Data> candidates = new ArrayList<>();

        for(final DataProvider provider : getProviders()){
            final Data layer = provider.get(key, version);
            if(layer != null) {
                candidates.add(layer);
            }
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(Data.class.isAssignableFrom(valClass)){
                //make a more accurate search testing both namespace and local part are the same.
                final GenericName nk = (GenericName) key;
                for(int i=0;i<candidates.size();i++){
                    final Data ld = candidates.get(i);
                    if(Objects.equals(NamesExt.getNamespace(ld.getName()), NamesExt.getNamespace(nk))
                            && Objects.equals(ld.getName().tip().toString(), nk.tip().toString())){
                        return ld;
                    }
                }

                //we could not find one more accurate then another
                return candidates.get(0);
            }else{
                return candidates.get(0);
            }
        }

        return null;
    }

    public Data get(final GenericName key, final String providerID, final Date version) {
        final DataProvider provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        if (version != null) {
            return provider.get(key, version);
        }
        return provider.get(key);
    }

    public Collection<DataProviderFactory> getFactories() {
        return FACTORIES;
    }

    public DataProviderFactory getFactory(final String factoryID) {
        for (DataProviderFactory serv : FACTORIES) {
            if (serv.getName().equals(factoryID)) {
                return serv;
            }
        }
        return null;
    }

    public Data getByIdentifier(GenericName key) {
        Data result = null;
        for(final GenericName n : getKeys()){
            if(n.equals(key)){
                return get(n);
            } else if (NamesExt.match(n, key)) {
                result = get(n);
            }
        }
        return result;
    }

    /**
     * Returns the current instance of {@link DataProviders}.
     */
    public static DataProviders getInstance(){
        return INSTANCE;
    }
    
}
