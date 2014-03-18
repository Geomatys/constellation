/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2011, Geomatys
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderParameters;
import org.apache.sis.util.NullArgumentException;
import org.apache.sis.util.logging.Logging;
import static org.constellation.provider.Provider.RELOAD_TIME_PROPERTY;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Common base class for LayerProviderProxy and StyleProviderProxy.
 * Ensure correct thread-safe reloading of providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProviderProxy<K,V,P extends Provider<K,V>, S
        extends ProviderService<K,V,P>> implements PropertyChangeListener{

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private long lastUpdateTime = System.currentTimeMillis();
    protected final Class<K> keyClass;
    protected final Class<V> valClass;
    
    //all loaded providers
    private Collection<P> PROVIDERS = null;
    private Configurator configurator = Configurator.DEFAULT;

    protected AbstractProviderProxy(Class<K> keyClass, Class<V> valClass){
        this.keyClass = keyClass;
        this.valClass = valClass;
    }

    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(K key) {
        return getKeys().contains(key);
    }

    /**
     * Empty implementation.
     */
    public void remove(K key) {
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
            saveConfiguration((S) provider.getService());
        }
        //forward events
        fireUpdateEvent();
    }

    /**
     * Set the object responsible to create Provider configurations.
     * @param configurator
     */
    public synchronized void setConfigurator(Configurator configurator) {
        if(configurator == null){
            throw new NullArgumentException("Configurator can not be null.");
        }

        if(this.configurator.equals(configurator)){
            //same configuration
            return;
        }

        //clear cache
        dispose();
        this.configurator = configurator;
    }

    public synchronized Configurator getConfigurator() {
        return configurator;
    }

    public P createProvider(final S service, final ParameterValueGroup params){
        getProviders();
        final P provider = service.createProvider(params);

        //add in the list our provider
        provider.addPropertyListener(this);
        PROVIDERS.add(provider);
        saveConfiguration(service);
        fireUpdateEvent();
        return provider;
    }

    public P removeProvider(final P provider){
        final boolean b = PROVIDERS.remove(provider);

        if(b){
            provider.removePropertyListener(this);
            saveConfiguration((S) provider.getService());
            fireUpdateEvent();
        }

        return provider;
    }

    /**
     * Save configuration for the given provider service
     */
    private void saveConfiguration(final S service){
        getLogger().log(Level.INFO, "Saving configuration for service : {0}", service.getName());
        //save configuration
        final List<Provider> providers = new ArrayList<>();
        for(P candidate : PROVIDERS){
            if(candidate.getService().equals(service)){
                providers.add(candidate);
            }
        }
        getConfigurator().saveConfiguration(service, providers);
    }

    /**
     * {@inheritDoc }
     */
    public Set<K> getKeys() {
        return getKeys(null);
    }

    /**
     * {@inheritDoc }
     */
    public Set<K> getKeys(final String sourceId) {
        final Set<K> keys = new HashSet<>();
        for(final Provider<K,V> provider : getProviders()){
            keys.addAll(provider.getKeys(sourceId));
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     * @deprecated use get(key, providerID) instead because two provider can have the same named layer
     */
    @Deprecated
    public V get(final K key) {
        final List<V> candidates = new ArrayList<>();

        for(final Provider<K,V> provider : getProviders()){
            final V layer = provider.get(key);
            if(layer != null) candidates.add(layer);
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(LayerDetails.class.isAssignableFrom(valClass)){
                //make a more accurate search testing both namespace and local part are the same.
                final Name nk = (Name) key;
                for(int i=0;i<candidates.size();i++){
                    final LayerDetails ld = (LayerDetails) candidates.get(i);
                    if(Objects.equals(ld.getName().getNamespaceURI(), nk.getNamespaceURI())
                            && Objects.equals(ld.getName().getLocalPart(), nk.getLocalPart())) {
                        return (V)ld;
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

    public V get(final K key, final String providerID) {
        final Provider<K,V> provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        return provider.get(key);
    }

    public List<V> getAll() {
        final List<V> values = new ArrayList<>();
        for(Provider<K,V> provider : getProviders()){
            for(K key : provider.getKeys()){
                values.add(provider.get(key));
            }
        }
        return values;
    }

    public abstract Collection<? extends S> getServices();

    public synchronized Collection<P> getProviders(){
        if(PROVIDERS != null){
            return Collections.unmodifiableCollection(PROVIDERS);
        }

        final Configurator configs = getConfigurator();
        final List<P> cache = new ArrayList<>();
        for(final ProviderService factory : getServices()){
            final String serviceName = factory.getName();

            //load configurable sources
            try{
                final ParameterValueGroup config = configs.getConfiguration(factory);
                if(config != null){
                    for(final ParameterValueGroup src : ProviderParameters.getSources(config)){
                        try{
                            final P prov = (P) factory.createProvider(src);
                            if(prov != null){
                                prov.addPropertyListener(this);
                                cache.add(prov);
                            }
                        }catch(Exception ex){
                            //we must not fail here in any case
                            getLogger().log(Level.SEVERE, "Service "+serviceName+" failed to create a provider.",ex);
                        }
                    }
                }
            }catch(Exception ex){
                //we must not fail here in any case
                getLogger().log(Level.SEVERE, "Configurator failed to provide configuration for service : " + serviceName,ex);
            }

            //load hard coded sources
            try{
                cache.addAll(factory.getAdditionalProviders());
            }catch(Exception ex){
                //we must not fail here in any case
                getLogger().log(Level.SEVERE, "Service "+serviceName+" failed to create additional providers.",ex);
            }
        }

        PROVIDERS = cache;
        fireUpdateEvent();
        return Collections.unmodifiableCollection(PROVIDERS);
    }

    public synchronized P getProvider(final String id){
        for (P provider : getProviders()) {
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
            //services are not loaded
            return;
        }

        try{
            //services were loaded, dispose each of them
            for(final Provider<K,V> provider : getProviders()){
                try{
                    provider.removePropertyListener(this);
                    provider.dispose();
                }catch(Exception ex){
                    //we must not fail here in any case
                    getLogger().log(Level.SEVERE, "Failed to dispose provider : " + provider.toString(),ex);
                }
            }
        }finally{
            //there should not be an error, but in worse case ensure this is correctly set to null.
            PROVIDERS = null;
        }
    }

}
