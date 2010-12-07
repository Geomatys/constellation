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

import java.util.Collections;
import java.util.Set;
import org.constellation.provider.configuration.ProviderSource;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProvider<K,V> implements Provider<K, V>{

    protected final ProviderSource source;

    public AbstractProvider(ProviderSource source){
        this.source = source;
    }

    @Override
    public ProviderSource getSource() {
        return source;
    }

    @Override
    public Set<K> getKeys(String sourceId) {
        if(sourceId != null && source != null){
            if(sourceId.equals(source.id)){
                return getKeys();
            }else{
                return Collections.emptySet();
            }
        }
        return getKeys();
    }

    @Override
    public boolean contains(K key) {
        return getKeys().contains(key);
    }

    @Override
    public void reload() {
    }

    @Override
    public void dispose() {
    }

}
