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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.NullArgumentException;
import org.apache.sis.util.logging.Logging;
import static org.constellation.provider.Provider.RELOAD_TIME_PROPERTY;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public final class StyleProviders implements PropertyChangeListener{

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.provider");

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private long lastUpdateTime = System.currentTimeMillis();
    protected final Class<String> keyClass = String.class;
    protected final Class<MutableStyle> valClass = MutableStyle.class;
    
    //all loaded providers
    private Collection<StyleProvider> PROVIDERS = null;
    private Configurator configurator = Configurator.DEFAULT;


    protected Logger getLogger() {
        return LOGGER;
    }

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
            saveConfiguration((StyleProviderFactory) provider.getService());
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

    public StyleProvider createProvider(final StyleProviderFactory service, final ParameterValueGroup params){
        getProviders();
        final StyleProvider provider = service.createProvider(params);

        //add in the list our provider
        provider.addPropertyListener(this);
        PROVIDERS.add(provider);
        saveConfiguration(service);
        fireUpdateEvent();
        return provider;
    }

    public StyleProvider removeProvider(final StyleProvider provider){
        final boolean b = PROVIDERS.remove(provider);

        if(b){
            provider.removePropertyListener(this);
            saveConfiguration(provider.getService());
            fireUpdateEvent();
        }

        return provider;
    }

    /**
     * Save configuration for the given provider service
     */
    private void saveConfiguration(final ProviderService service){
        getLogger().log(Level.INFO, "Saving configuration for service : {0}", service.getName());
        //save configuration
        final List<Provider> providers = new ArrayList<>();
        for(StyleProvider candidate : PROVIDERS){
            if(candidate.getService().equals(service)){
                providers.add(candidate);
            }
        }
        getConfigurator().saveConfiguration(service, providers);
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

        final Configurator configs = getConfigurator();
        final List<StyleProvider> cache = new ArrayList<>();
        for(final ProviderService factory : getServices()){
            final String serviceName = factory.getName();

            //load configurable sources
            try{
                final ParameterValueGroup config = configs.getConfiguration(factory);
                if(config != null){
                    for(final ParameterValueGroup src : ProviderParameters.getSources(config)){
                        try{
                            final StyleProvider prov = (StyleProvider) factory.createProvider(src);
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
            //services are not loaded
            return;
        }

        try{
            //services were loaded, dispose each of them
            for(final Provider<String,MutableStyle> provider : getProviders()){
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

    public static final MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)
            FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
    public final FilterFactory2 FILTER_FACTORY = (FilterFactory2)FactoryFinder.getFilterFactory(
                            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

    private static final Collection<StyleProviderFactory> SERVICES;
    static {
        final List<StyleProviderFactory> cache = new ArrayList<>();
        final ServiceLoader<StyleProviderFactory> loader = ServiceLoader.load(StyleProviderFactory.class);
        for(final StyleProviderFactory service : loader){
            cache.add(service);
        }
        SERVICES = Collections.unmodifiableCollection(cache);
    }

    private static final StyleProviders INSTANCE = new StyleProviders();

    public Collection<StyleProviderFactory> getServices() {
        return SERVICES;
    }

    /**
     * Returns the current instance of {@link StyleProviders}.
     */
    public static StyleProviders getInstance(){
        return INSTANCE;
    }

}
