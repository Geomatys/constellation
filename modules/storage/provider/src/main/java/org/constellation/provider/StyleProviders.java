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

import org.constellation.configuration.ConfigurationException;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.utility.parameter.ParametersExt;
import org.opengis.filter.FilterFactory2;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;

import static org.constellation.provider.Provider.RELOAD_TIME_PROPERTY;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
@Deprecated
public final class StyleProviders extends Providers implements PropertyChangeListener{

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private long lastUpdateTime = System.currentTimeMillis();
    protected final Class<String> keyClass = String.class;
    protected final Class<MutableStyle> valClass = MutableStyle.class;
    
    //all loaded providers
    private Collection<StyleProvider> PROVIDERS = null;

    /**
     * {@inheritDoc}
     */
    public boolean contains(String key) {
        return getKeys().contains(key);
    }

    /**
     * Empty implementation.
     */
    public void remove(String key) {
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

    public StyleProvider createProvider(final String providerId, final StyleProviderFactory factory, 
            final ParameterValueGroup params) throws ConfigurationException{
        getProviders();
        final StyleProvider provider = factory.createProvider(providerId,params);

        //add in the list our provider
        provider.addPropertyListener(this);
        PROVIDERS.add(provider);
        //save the configuration
        getConfigurator().addProviderConfiguration(providerId,params,null);
        fireUpdateEvent();
        return provider;
    }

    public StyleProvider removeProvider(final StyleProvider provider) throws ConfigurationException{
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
    public Set<String> getKeys() {
        return getKeys(null);
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys(final String sourceId) {
        final Set<String> keys = new HashSet<>();
        for(final Provider<String,MutableStyle> provider : getProviders()){
            keys.addAll(provider.getKeys(sourceId));
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     * @deprecated use get(key, providerID) instead because two provider can have the same named layer
     */
    @Deprecated
    public MutableStyle get(final String key) {
        final List<MutableStyle> candidates = new ArrayList<>();

        for(final Provider<String,MutableStyle> provider : getProviders()){
            final MutableStyle layer = provider.get(key);
            if(layer != null) candidates.add(layer);
        }

        if(candidates.size() >= 1){
            return candidates.get(0);
        }

        return null;
    }

    public MutableStyle get(final String key, final String providerID) {
        final Provider<String,MutableStyle> provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        return provider.get(key);
    }

    public List<MutableStyle> getAll() {
        final List<MutableStyle> values = new ArrayList<>();
        for(Provider<String,MutableStyle> provider : getProviders()){
            for(String key : provider.getKeys()){
                values.add(provider.get(key));
            }
        }
        return values;
    }

    public synchronized Collection<StyleProvider> getProviders(){
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
        
        final List<StyleProvider> cache = new ArrayList<>();
        
        //rebuild providers
        for(Entry<String,ParameterValueGroup> entry : configs){
            final String providerId = entry.getKey();
            final ParameterValueGroup params = entry.getValue();
                
            for(final ProviderFactory factory : getFactories()){
                //check if config can be used by this factory
                final String paramName = factory.getStoreDescriptor().getName().getCode();
                final GeneralParameterValue param = ParametersExt.getParameter(params, paramName);
                if(param!=null){
                    try{
                        final StyleProvider prov = (StyleProvider)factory.createProvider(providerId, params);
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

    public synchronized StyleProvider getProvider(final String id){
        for (StyleProvider provider : getProviders()) {
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
            //providers were loaded, dispose each of them
            for(final Provider<String,MutableStyle> provider : getProviders()){
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

    public static final MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)
            FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    public final FilterFactory2 FILTER_FACTORY = (FilterFactory2)FactoryFinder.getFilterFactory(
                            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

    private static final Collection<StyleProviderFactory> FACTORIES;
    static {
        final List<StyleProviderFactory> cache = new ArrayList<>();
        final ServiceLoader<StyleProviderFactory> loader = ServiceLoader.load(StyleProviderFactory.class);
        for(final StyleProviderFactory factory : loader){
            cache.add(factory);
        }
        FACTORIES = Collections.unmodifiableCollection(cache);
    }

    private static final StyleProviders INSTANCE = new StyleProviders();

    public Collection<StyleProviderFactory> getFactories() {
        return FACTORIES;
    }

    public StyleProviderFactory getFactory(final String factoryID) {
        for (StyleProviderFactory serv : FACTORIES) {
            if (serv.getName().equals(factoryID)) {
                return serv;
            }
        }
        return null;
    }
    /**
     * Returns the current instance of {@link StyleProviders}.
     */
    public static StyleProviders getInstance(){
        return INSTANCE;
    }

}
