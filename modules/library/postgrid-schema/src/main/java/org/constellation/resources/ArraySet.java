/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.resources;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.AbstractSet;
import java.util.NoSuchElementException;
import java.io.Serializable;

import org.geotoolkit.util.Utilities;


/**
 * An immutable set backed by an array. It is the user responsability to ensure
 * that the set do not contains duplicated elements.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ArraySet<Element> extends AbstractSet<Element> implements Serializable {
    /**
     * Serial number for compatibility between different versions.
     */
    private static final long serialVersionUID = -8316857135034826970L;

    /**
     * The elements.
     */
    private final Element[] elements;

    /**
     * Constructs a set from an arbitrary collection. This is the caller responsability
     * to ensure that this collection do not contains any duplicated elements. It will
     * be checked only if assertions are enabled.
     */
    @SuppressWarnings("unchecked")
    public ArraySet(final Collection<Element> elements) {
        this((Element[])elements.toArray());
    }

    /**
     * Constructs a set initialized with the specified array. <strong>This array is not
     * cloned</strong>.  Consequently, it should not be modified externally after this
     * object is constructed. Note that null elements in this array may be changed later
     * if the {@link #create} method has been overrided.
     */
    public ArraySet(final Element[] elements) {
        this.elements = elements;
        assert !hasDuplicated(elements);
    }

    /**
     * Returns {@code true} if the specified array contains duplicated elements.
     * If the array contains one or more {@code null} elements, then this method
     * conservatively returns {@code false}  since the creation of null elements
     * may be differed at a later time with the {@link #create} method. This method
     * is used for assertions only.
     */
    private boolean hasDuplicated(final Element[] elements) {
        final Set<Element> set = new HashSet<Element>(Arrays.asList(elements));
        if (set.remove(null)) {
            return false;
        }
        return set.size() != elements.length;
    }

    /**
     * Returns the number of elements in this collection.
     */
    public int size() {
        return elements.length;
    }

    /**
     * Returns an iterator over the elements in this collection. If the method {@link #create}
     * has been overriden, then the element may be created on the fly during the iteration.
     */
    public Iterator<Element> iterator() {
        return new Iterator<Element>() {
            private int index = 0;

            public boolean hasNext() {
                return index < elements.length;
            }

            public Element next() {
                if (index >= elements.length) { throw new NoSuchElementException();}
                if (elements[index] == null)  { elements[index] = create(index);   }
                return elements[index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     */
    @Override
    public boolean contains(final Object e) {
        for (int i=0; i<elements.length; i++) {
            if (elements[i] == null) {
                elements[i] = create(i);
            }
            if (Utilities.equals(e, elements[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an array containing all of the elements in this collection.
     */
    @Override
    public Object[] toArray() {
        return elements.clone();
    }

    /**
     * Invoked when the {@linkplain #iterator iterator} pass over a null element. If this method
     * returns a non-null value, then this value will be stored in the array wrapped by this
     * {@code ArraySet}. This method gives a chance to create element only when first needed.
     * The default implementation returns always {@code null}.
     */
    protected Element create(final int index) {
        return null;
    }
}
