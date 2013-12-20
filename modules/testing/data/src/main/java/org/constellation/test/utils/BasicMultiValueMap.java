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

package org.constellation.test.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BasicMultiValueMap<K,V> extends HashMap<K, List<V>> implements MultivaluedMap<K, V>{

    @Override
    public void putSingle(K k, V v) {
        final List<V> values = new ArrayList<>();
        values.add(v);
        put(k, values);
    }

    @Override
    public void add(K k, V v) {
        List<V> values = get(k);
        if(values == null){
            putSingle(k, v);
        }else{
            values.add(v);
        }
    }

    @Override
    public V getFirst(K k) {
        List<V> values = get(k);
        if(values == null || values.isEmpty()){
            return null;
        }else{
            return values.get(0);
        }
    }

    @Override
    public void addAll(K k, V... vs) {
        put(k,Arrays.asList(vs));
    }

    @Override
    public void addAll(K k, List<V> list) {
        put(k, list);
    }

    @Override
    public void addFirst(K k, V v) {
        List<V> values = get(k);
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(0, v);
        put(k, values);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> mm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
