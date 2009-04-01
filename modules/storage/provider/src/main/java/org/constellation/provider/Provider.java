/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

import java.util.Set;

/**
 * A dataprovider is basicly a index class  
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Provider<K,V> {

    public final String JNDI_GROUP = "Data Provider Properties";
    
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
     * If you want to intend to get the related data, you should use the
     * get method directly and test if the result is not null.
     * 
     * @return true if the given key data is in this data provider .
     */
    boolean contains(K key);
    
    /**
     * Get the data related to the given key.
     * @return V object if it is in the dataprovider, or null if not.
     */
    V get(K key);
    
    /**
     * Reload data provider. this may be usefull if new entries on disk have been
     * added after creation.
     */
    void reload();
    
    /**
     * Clear every caches, this dataprovider should not be used after a call
     * to this method.
     */
    void dispose();
    
}
