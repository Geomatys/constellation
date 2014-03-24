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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProvider<K,V> implements Provider<K, V>{

    protected static final Logger LOGGER = Providers.LOGGER;

    //configuration
    protected final ProviderFactory<K, V, Provider<K, V>> service;
    protected final String id;
    private ParameterValueGroup source;
    
    //listeners
    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private long lastUpdateTime = System.currentTimeMillis();

    public AbstractProvider(final String id, final ProviderFactory<K, V, Provider<K, V>> service, final ParameterValueGroup source){
        this.id = id;
        this.source = source;
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId(){
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderFactory<K, V, Provider<K, V>> getFactory() {
        return service;
    }



    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ParameterValueGroup getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void updateSource(ParameterValueGroup config){
        if(!source.getDescriptor().equals(config.getDescriptor())){
            throw new IllegalArgumentException("New parameters or not of the same type");
        }
        this.source = config;
        reload();
        fireUpdateEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> getKeys(String sourceId) {
        if(sourceId != null && source != null){
            if(sourceId.equals(getId())){
                return getKeys();
            }else{
                return Collections.emptySet();
            }
        }
        return getKeys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(K key) {
        return getKeys().contains(key);
    }

    /**
     * Empty implementation.
     */
    @Override
    public void reload() {
    }

    /**
     * Empty implementation.
     */
    @Override
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll() {
        for (K key : getKeys()) {
            remove(key);
        }
    }

    /**
     * Empty implementation.
     */
    @Override
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
    @Override
    public void addPropertyListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

}
