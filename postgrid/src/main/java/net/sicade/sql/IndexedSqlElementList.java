/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.sql;

import java.util.AbstractSequentialList;
import java.util.ListIterator;


/**
 * A list wrapping an array of {@link IndexedSqlElement}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class IndexedSqlElementList<E extends IndexedSqlElement> extends AbstractSequentialList<E> {
    /**
     * The query type for which this list is created.
     */
    private final QueryType type;

    /**
     * {@link Query#columns} or {@link Query#parameters} at the time this list has been created.
     */
    private final E[] elements;

    /**
     * Creates a list for the given query type.
     */
    IndexedSqlElementList(final QueryType type, final E[] elements) {
        this.type = type;
        this.elements = elements;
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     */
    @Override
    public boolean isEmpty() {
        for (final E c : elements) {
            if (c.indexOf(type) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of elements in this collection.
     */
    public int size() {
        int count = 0;
        for (final E c : elements) {
            if (c.indexOf(type) != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a list iterator over the elements in this list.
     */
    public ListIterator<E> listIterator(final int index) {
        return new IndexedSqlElementIterator<E>(type, elements, index);
    }
}
