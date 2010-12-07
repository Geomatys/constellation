/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.util.NullArgumentException;

/**
 * Commun base class for LayerProviderProxy and StyleProviderProxy.
 * Ensure correct thread-safe reloading of providers.
 *
 * @author Johann Sorel (Geomatys)
 */
abstract class AbstractProviderProxy<K,V,P extends Provider<K,V>, S
        extends ProviderService<K,V,P>> extends AbstractProvider<K, V>{

    //all loaded providers
    private Collection<P> PROVIDERS = null;
    private Configurator configurator = Configurator.DEFAULT;

    protected AbstractProviderProxy(){
        super(null);
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

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<K> getKeys() {
        return getKeys(null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<K> getKeys(String sourceId) {
        final Set<K> keys = new HashSet<K>();
        for(final Provider<K,V> provider : getProviders()){
            keys.addAll(provider.getKeys(sourceId));
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public V get(K key) {
        for(final Provider<K,V> provider : getProviders()){
            final V layer = provider.get(key);
            if(layer != null) return layer;
        }
        return null;
    }

    protected abstract Collection<? extends S> getServices();

    public synchronized Collection<P> getProviders(){
        if(PROVIDERS != null){
            return PROVIDERS;
        }

        final Configurator configs = getConfigurator();
        final List<P> cache = new ArrayList<P>();
        for(final ProviderService factory : getServices()){
            final String serviceName = factory.getName();

            //load configurable sources
            final ProviderConfig config = configs.getConfiguration(serviceName);
            for(final ProviderSource src : config.sources){
                final P prov = (P) factory.createProvider(src);
                if(prov != null){
                    cache.add(prov);
                }
            }

            //load hard coded sources
            cache.addAll(factory.getAdditionalProviders());
        }

        PROVIDERS = Collections.unmodifiableCollection(cache);
        return PROVIDERS;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void reload() {
        dispose();
        getProviders(); //will load providers
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void dispose() {
        if(PROVIDERS == null){
            //services are not loaded
            return;
        }

        try{
            //services were loaded, dispose each of them
            for(final Provider<K,V> provider : getProviders()){
                provider.dispose();
            }
        }finally{
            //there should not be an error, but in worse case ensure this is correctly set to null.
            PROVIDERS = null;
        }
    }

}
