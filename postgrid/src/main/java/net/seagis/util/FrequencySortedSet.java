/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;


/**
 * A set with elements ordered by the amount of time they were {@linkplain #add added}.
 * Less frequently added elements are first, and most frequently added ones are last.
 * If some elements were added the same amount of time, then the iterator will traverse
 * them in their insertion order.
 * <p>
 * An optional boolean argument in the constructor allows the construction of set in reversed
 * order (most frequently added elements first, less frequently added last). This is similar
 * but not identical to creating a defaut {@code FrequencySortedSet} and iterating through it
 * in reverse order. The difference is that is elements added the same amount of time will still
 * be traversed in their insertion order.
 *
 * @since 2.5
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FrequencySortedSet<E> extends AbstractSet<E> implements SortedSet<E>, Comparator<E>, Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6034102231354388179L;

    /**
     * The frequency of occurence for each element. We must use a linked hash map instead of an
     * ordinary hash map because we want to preserve insertion order for elements that occur at
     * the same frequency.
     */
    private final Map<E,Integer> count = new LinkedHashMap<E,Integer>();

    /**
     * {@code +1} if the element should be sorted in the usual order, or {@code -1}
     * if the elements should be sorted in reverse order (most frequent element first).
     */
    private final int order;

    /**
     * Elements in sorted order, or {@code null} if not yet computed.
     */
    private transient List<E> sorted;

    /**
     * The frequency for each {@linkplain #sorted} element. This array is invalid
     * if {@link #sorted} is null.
     */
    private transient int[] frequencies;

    /**
     * Creates an initially empty set with less frequent elements first.
     */
    public FrequencySortedSet() {
        this(false);
    }

    /**
     * Creates an initially empty set.
     *
     * @param reversed {@code true} if the elements should be sorted in reverse order
     *                 (most frequent element first, less frequent last).
     */
    public FrequencySortedSet(final boolean reversed) {
        order = reversed ? -1 : +1;
    }

    /**
     * Returns the number of elements in this set.
     */
    public int size() {
        return count.size();
    }

    /**
     * Returns {@code true} if this set is empty.
     */
    @Override
    public boolean isEmpty() {
        return count.isEmpty();
    }

    /**
     * Adds the specified element to this set. Returns {@code true} if this set changed as a
     * result of this operation. Changes in element order are not notified by the returned
     * value.
     */
    @Override
    public boolean add(final E element) {
        sorted = null;
        final Integer n = count.put(element, order);
        if (n == null) {
            return true;
        }
        if (count.put(element, n + order) != n) {
            throw new AssertionError(element);
        }
        return false;
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        return count.containsKey(o);
    }

    /**
     * Removes the specified element from this set, no matter how many time it has been added.
     * Returns {@code true} if this set changed as a result of this operation.
     */
    @Override
    public boolean remove(final Object element) {
        if (count.remove(element) != null) {
            sorted = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all elements from this set.
     */
    @Override
    public void clear() {
        sorted = null;
        count.clear();
    }

    /**
     * Returns an iterator over the elements in this set in frequency order.
     */
    public Iterator<E> iterator() {
        ensureSorted();
        return sorted.iterator();
    }

    /**
     * @todo Not yet implemented.
     */
    public SortedSet<E> headSet(E toElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @todo Not yet implemented.
     */
    public SortedSet<E> tailSet(E fromElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @todo Not yet implemented.
     */
    public SortedSet<E> subSet(E fromElement, E toElement) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the first element in this set.
     * <ul>
     *   <li>For sets created with the default order, this is the less frequently added element.
     *       If more than one element were added with the same frequency, this is the first one
     *       that has been {@linkplain #added} to this set at this frequency.</li>
     *   <li>For sets created with the reverse order, this is the most frequently added element.
     *       If more than one element were added with the same frequency, this is the first one
     *       that has been {@linkplain #added} to this set at this frequency.</li>
     * </ul>
     *
     * @throws NoSuchElementException if this set is empty.
     */
    public E first() throws NoSuchElementException {
        ensureSorted();
        final int length = sorted.size();
        if (length != 0) {
            return sorted.get(0);
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the last element in this set.
     * <ul>
     *   <li>For sets created with the default order, this is the most frequently added element.
     *       If more than one element were added with the same frequency, this is the last one
     *       that has been {@linkplain #added} to this set at this frequency.</li>
     *   <li>For sets created with the reverse order, this is the less frequently added element.
     *       If more than one element were added with the same frequency, this is the last one
     *       that has been {@linkplain #added} to this set at this frequency.</li>
     * </ul>
     *
     * @throws NoSuchElementException if this set is empty.
     */
    public E last() throws NoSuchElementException {
        ensureSorted();
        final int length = sorted.size();
        if (length != 0) {
            return sorted.get(length - 1);
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Sorts the elements in frequency order, if not already done. The sorted array will contains
     * all elements without duplicated values, with the less frequent element first and the most
     * frequent last (or the converse if this set has been created for reverse order). If some
     * elements appear at the same frequency, then their ordering will be preserved.
     */
    private void ensureSorted() {
        if (sorted != null) {
            return;
        }
        @SuppressWarnings("unchecked")
        final Map.Entry<E,Integer>[] entries = count.entrySet().toArray(new Map.Entry[count.size()]);
        Arrays.sort(entries, COMPARATOR);
        final int length = entries.length;

        @SuppressWarnings("unchecked")
        final E[] keys = (E[]) new Object[length];
        if (frequencies == null || frequencies.length != length) {
            frequencies = new int[length];
        }
        for (int i=0; i<length; i++) {
            final Map.Entry<E,Integer> entry = entries[i];
            keys[i] = entry.getKey();
            frequencies[i] = Math.abs(entry.getValue());
        }
        sorted = Arrays.asList(keys);
    }

    /**
     * The comparator used for sorting map entries. Most be consistent with
     * {@link #compare} implementation.
     */
    private static final Comparator<Map.Entry<?,Integer>> COMPARATOR = new Comparator<Map.Entry<?,Integer>>() {
        public int compare(Map.Entry<?,Integer> o1, Map.Entry<?,Integer> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    };

    /**
     * Returns the comparator used to order the elements in this set. For a
     * {@code FrequencySortedSet}, the comparator is always {@code this}.
     * <p>
     * This method is final because the {@code FrequencySortedSet} implementation makes
     * assumptions on the comparator that would not hold if this method were overrided.
     */
    public final Comparator<E> comparator() {
        return this;
    }

    /**
     * Compares the specified elements for {@linkplain #frequency frequency}. For
     * {@code FrequencySortedSet} with default ordering, this method returns a positive
     * number if {@code o1} has been added more frequently to this set than {@code o2},
     * a negative number if {@code o1} has been added less frequently than {@code o2},
     * and 0 otherwise. For {@code FrequencySortedSet} with reverse ordering, this is the
     * converse.
     * <p>
     * This method is final because the {@code FrequencySortedSet} implementation makes
     * assumptions on the comparator that would not hold if this method were overrided.
     */
    public final int compare(final E o1, final E o2) {
        return signedFrequency(o1) - signedFrequency(o2);
    }

    /**
     * Returns the frequency of the specified element in this set. Returns
     * a negative number if this set has been created for reversed order.
     */
    private int signedFrequency(final E element) {
        final Integer n = count.get(element);
        return (n != null) ? n : 0;
    }

    /**
     * Returns the frequency of the specified element in this set.
     */
    public int frequency(final E element) {
        return Math.abs(signedFrequency(element));
    }

    /**
     * Returns the frequency of each element in this set, in iteration order.
     */
    public int[] frequencies() {
        ensureSorted();
        return frequencies.clone();
    }

    /**
     * Returns the content of this set as an array.
     */
    @Override
    public Object[] toArray() {
        ensureSorted();
        return sorted.toArray();
    }

    /**
     * Returns the content of this set as an array.
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        ensureSorted();
        return sorted.toArray(array);
    }
}
