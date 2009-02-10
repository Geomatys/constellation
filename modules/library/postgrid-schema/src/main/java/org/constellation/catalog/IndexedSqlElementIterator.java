/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.catalog;

import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * An iterator over an array of {@link IndexedSqlElement}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class IndexedSqlElementIterator<E extends IndexedSqlElement> implements ListIterator<E> {
    /**
     * The query type for which this iterator is created.
     */
    private final QueryType type;

    /**
     * {@link Query#columns} or {@link Query#parameters} at the time this iterator has been created.
     */
    private final E[] elements;

    /**
     * The elements to be returned by {@link #previous()} and {@link #next()} methods.
     */
    private E previous, next;

    /**
     * The index of next element on {@link #elements}.
     */
    private int elementIndex;

    /**
     * The iterator index returned by public methods.
     */
    public int iteratorIndex;

    /**
     * Creates an iterator for the given query type.
     */
    IndexedSqlElementIterator(final QueryType type, final E[] elements, int index) {
        this.type = type;
        this.elements = elements;
        iteratorIndex = index;
        if (index >= 0) {
            while (elementIndex < elements.length) {
                final E candidate = elements[elementIndex];
                if (candidate.indexOf(type) != 0) {
                    previous = next;
                    next = candidate;
                    if (--index < 0) {
                        return;
                    }
                }
                elementIndex++;
            }
            previous = next;
            next = null;
        }
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(iteratorIndex));
        }
    }

    /**
     * Returns {@code true} if this iterator has more elements in the forward direction.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Returns the next element in the list.
     */
    public E next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        previous = next;
        next = null;
        while (++elementIndex < elements.length) {
            final E candidate = elements[elementIndex];
            if (candidate.indexOf(type) != 0) {
                next = candidate;
                break;
            }
        }
        iteratorIndex++;
        assert previous.indexOf(type) == iteratorIndex : previous;
        return previous;
    }

    /**
     * Returns {@code true} if this list iterator has more elements in the reverse direction.
     */
    public boolean hasPrevious() {
        return previous != null;
    }

    /**
     * Returns the previous element in the list.
     */
    public E previous() {
        if (previous == null) {
            throw new NoSuchElementException();
        }
        next = previous;
        previous = null;
        while (--elementIndex > 0) {
            final E candidate = elements[elementIndex - 1];
            if (candidate.indexOf(type) != 0) {
                previous = candidate;
                break;
            }
        }
        iteratorIndex--;
        return next;
    }

    /**
     * The index of the next element.
     */
    public int nextIndex() {
        return iteratorIndex;
    }

    /**
     * The index of the previous element.
     */
    public int previousIndex() {
        return iteratorIndex - 1;
    }

    /**
     * Unsupported operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     */
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     */
    public void add(E e) {
        throw new UnsupportedOperationException();
    }
}
