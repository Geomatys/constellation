/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.configuration.ConfigurationException;
import static org.constellation.provider.Provider.RELOAD_TIME_PROPERTY;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.ElevationModel;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

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
    protected final Class<Name> keyClass = Name.class;
    protected final Class<Data> valClass = Data.class;
    
    //all loaded providers
    private Collection<DataProvider> PROVIDERS = null;

    /**
     * {@inheritDoc}
     */
    public boolean contains(Name key) {
        return getKeys().contains(key);
    }

    /**
     * Empty implementation.
     */
    public void remove(Name key) {
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

    public DataProvider createProvider(String id, final DataProviderFactory factory, 
            final ParameterValueGroup params) throws ConfigurationException{
        getProviders();
        
        final DataProvider provider = factory.createProvider(id,params);
        //add in the list our provider
        provider.addPropertyListener(this);
        PROVIDERS.add(provider);
        //save the configuration
        getConfigurator().addProviderConfiguration(id,params);
        fireUpdateEvent();
        return provider;
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
    public Set<Name> getKeys() {
        return getKeys(null);
    }

    /**
     * {@inheritDoc }
     */
    public Set<Name> getKeys(final String sourceId) {
        final Set<Name> keys = new HashSet<>();
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
    public Data get(final Name key) {
        final List<Data> candidates = new ArrayList<>();

        for(final Provider<Name,Data> provider : getProviders()){
            final Data layer = provider.get(key);
            if(layer != null) candidates.add(layer);
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(Data.class.isAssignableFrom(valClass)){
                //make a more accurate search testing both namespace and local part are the same.
                final Name nk = (Name) key;
                for(int i=0;i<candidates.size();i++){
                    final Data ld = (Data) candidates.get(i);
                    if(Objects.equals(ld.getName().getNamespaceURI(), nk.getNamespaceURI())
                            && Objects.equals(ld.getName().getLocalPart(), nk.getLocalPart())) {
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

    public Data get(final Name key, final String providerID) {
        final Provider<Name,Data> provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        return provider.get(key);
    }

    public List<Data> getAll() {
        final List<Data> values = new ArrayList<>();
        for(Provider<Name,Data> provider : getProviders()){
            for(Name key : provider.getKeys()){
                values.add(provider.get(key));
            }
        }
        return values;
    }

    public synchronized Collection<DataProvider> getProviders(){
        if(PROVIDERS != null){
            return Collections.unmodifiableCollection(PROVIDERS);
        }

        final List<Entry<String,ParameterValueGroup>> configs;
        try {
            configs = getConfigurator().getProviderConfigurations();
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return Collections.EMPTY_LIST;
        }
        
        final List<DataProvider> cache = new ArrayList<>();
        
        //rebuild providers
        for(Entry<String,ParameterValueGroup> entry : configs){
            final String providerId = entry.getKey();
            final ParameterValueGroup params = entry.getValue();
                
            for(final ProviderFactory factory : getFactories()){
                //check if config can be used by this factory
                if(factory.canProcess(params)){
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
            for(final Provider<Name,Data> provider : getProviders()){
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
    public ElevationModel getElevationModel(final Name name) {
        for(final DataProvider provider : getProviders()){
            final ElevationModel model = provider.getElevationModel(name);
            if(model != null) return model;
        }
        return null;
    }

    public Data get(final Name key, final Date version) {
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
                final Name nk = (Name) key;
                for(int i=0;i<candidates.size();i++){
                    final Data ld = candidates.get(i);
                    if(Objects.equals(ld.getName().getNamespaceURI(), nk.getNamespaceURI())
                            && Objects.equals(ld.getName().getLocalPart(), nk.getLocalPart())){
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

    public Data get(final Name key, final String providerID, final Date version) {
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

    public Data getByIdentifier(Name key) {
        Data result = null;
        for(final Name n : getKeys()){
            if(n.equals(key)){
                return get(n);
            } else if (DefaultName.match(n, key)) {
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
