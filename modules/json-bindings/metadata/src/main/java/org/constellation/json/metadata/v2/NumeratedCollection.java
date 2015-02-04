
package org.constellation.json.metadata.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author guilhem
 */
public class NumeratedCollection implements Collection{

    private final Collection original;
    private final Map<Integer, Object> valueMap = new HashMap<>();
    
    public NumeratedCollection(final Collection original) {
        this.original = original;
    }
    
    public Object get(Integer i) {
        return valueMap.get(i);
    }
    
    public void put(Integer i, Object obj) {
        valueMap.put(i, obj);
    }
    
    public void replace(int ordinal, Object newValue) {
        if (original instanceof List) {
            List list = (List) original;
            list.set(ordinal, newValue);
        } else {
            final Iterator it = original.iterator();
            Object old = it.next();
            for (int i = 0; i < ordinal; i++) {
                old = it.next();
            }
            original.remove(old);
            original.add(newValue);
        }
    }
    
    @Override
    public int size() {
        return valueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return original.contains(o);
    }

    @Override
    public Iterator iterator() {
        return valueMap.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return original.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return original.toArray(a);
    }

    @Override
    public boolean add(Object e) {
        return original.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return original.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return original.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return original.addAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return original.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return original.retainAll(c);
    }

    @Override
    public void clear() {
        original.clear();
    }
    
}
