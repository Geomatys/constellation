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

package org.constellation.test.utils;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
