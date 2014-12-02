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

import org.opengis.parameter.ParameterValueGroup;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractProvider that = (AbstractProvider) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
