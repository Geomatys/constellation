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

import org.constellation.admin.dao.DataRecord.DataType;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.opengis.parameter.ParameterValueGroup;

import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 * A data provider is basically a index class
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public interface Provider<K,V> {

    final String RELOAD_TIME_PROPERTY = "updateTime";

    final String JNDI_GROUP = "Data Provider Properties";

    String getId();
            
    /**
     * @return the factory which created this provider.
     */
    ProviderFactory<K, V, Provider<K, V>> getFactory();

    /**
     * @return the Key class
     */
    Class<K> getKeyClass();
    
    /**
     * @return the Value class.
     */
    Class<V> getValueClass();
    
    /**
     * Use this method if you need the complete list of entries in this data provider.
     * If you are just searching if a special key exists than you should use the contains method.
     */
    Set<K> getKeys();

    /**
     * Use this method if you need the complete list of entries in this data provider for the specified source id.
     * If you are just searching if a special key exists than you should use the contains method.
     */
    Set<K> getKeys(String sourceId);
    
    /**
     * If you want to intend to get the related data, you should use the
     * get method directly and test if the result is not null.
     * 
     * @return true if the given key data is in this data provider .
     */
    boolean contains(K key);

    /**
     * Get the data related to the given key. the key is stored as a String here
     * for convenient needs. The service should be able to transform
     * the String key to it's own key class.
     * @return V object if it is in the data provider, or null if not.
     */
    V getByIdentifier(K key);

    /**
     * Get the data related to the given key.
     * @return V object if it is in the data provider, or null if not.
     */
    V get(K key);

    /**
     * Reload data provider. this may be useful if new entries on disk have been
     * added after creation.
     */
    void reload();
    
    /**
     * Clear every caches, this data provider should not be used after a call
     * to this method.
     */
    void dispose();

    /**
     * The configuration of this provider. Can be null if the provider
     * is hard coded.
     */
    ParameterValueGroup getSource();

    /**
     * Remove all data from this provider.
     */
    void removeAll();

    /**
     * Remove a data from this provider.
     *
     * @param key Data name to be removed from this provider.
     */
    void remove(K key);

    /**
     * Update the provider configuration.
     * 
     * @param config : new configuration
     */
    void updateSource(ParameterValueGroup config);

    /**
     * Add a property listener.
     */
    void addPropertyListener(PropertyChangeListener listener);

    /**
     * Remove a property listener.
     */
    void removePropertyListener(PropertyChangeListener listener);

    ProviderType getProviderType();

    DataType getDataType();
    
    boolean isSensorAffectable();
}
