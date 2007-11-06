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
package net.seagis.catalog;

import java.sql.ResultSet;          // For javadoc
import java.sql.PreparedStatement;  // For javadoc
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;


/**
 * Base class for SQL language elements accessible by index number.
 * The two main language elements that are concerned are:
 * <p>
 * <ul>
 *   <li>{@linkplain Column Columns}, which are accessible by index using
 *       {@link ResultSet#getString(int)}</li>
 *   <li>{@linkplain Parameter Parameters}, which are accessible by index using
 *       {@link PreparedStatement#setString(int, String)}</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class IndexedSqlElement {
    /**
     * The parameter index for each {@linkplain query type}. If a query type is not
     * supported by this element, then the corresponding index will be 0. Otherwise
     * index should be equals or greater than 1.
     */
    private final short[] index;

    /**
     * The functions to apply on this column or parameter. Will be created only if needed.
     */
    private EnumMap<QueryType,String> functions;

    /**
     * Creates a new language element for the specified query.
     *
     * @param query  The query for which the element is created.
     * @param types  The query types for which the element applies, or {@code null} for all
     *               {@code SELECT} queries (i.e. excluding {@link QueryType#INSERT INSERT}).
     */
    protected IndexedSqlElement(final Query query, final QueryType[] types) {
        /*
         * Creates the enum set from the 'types' array, and
         * computes the index array length in the same occasion.
         */
        final EnumSet<QueryType> typeSet;
        if (types == null) {
            typeSet = EnumSet.allOf(QueryType.class);
            typeSet.remove(QueryType.INSERT);
        } else {
            typeSet = EnumSet.noneOf(QueryType.class);
            for (final QueryType type : types) {
                typeSet.add(type);
            }
        }
        int length = 0;
        for (final QueryType type : typeSet) {
            final int ordinal = type.ordinal();
            if (ordinal >= length) {
                length = ordinal + 1;
            }
        }
        /*
         * Computes the index. For each QueryType supported by this language element, we scan
         * the previous elements until we find one supporting the same QueryType. The index
         * is then the previous index + columnSpan.
         */
        index = new short[length];
        if (query != null) {
            final IndexedSqlElement[] existingElements = query.add(this);
search:     for (final QueryType type : typeSet) {
                final int typeOrdinal = type.ordinal();
                for (int i=existingElements.length; --i>=0;) {
                    final IndexedSqlElement previous = existingElements[i];
                    if (typeOrdinal < previous.index.length) {
                        short position = previous.index[typeOrdinal];
                        if (position != 0) {
                            if (++position < 0) {
                                throw new IndexOutOfBoundsException(); // Overflow
                            }
                            index[typeOrdinal] = position;
                            continue search;
                        }
                    }
                }
                index[typeOrdinal] = 1;
            }
        }
    }

    /**
     * Returns the element ({@linkplain Column column} or {@linkplain Parameter parameter}) index
     * when used in a query of the given type. Valid index numbers start at 1. This method returns
     * 0 if this language element is not applicable to a query of the specified type.
     *
     * @param  type The query type.
     * @return The element index in the SQL prepared statment, or 0 if none.
     */
    int indexOf(final QueryType type) {
        final int ordinal = type.ordinal();
        if (ordinal>=0 && ordinal<index.length) {
            return index[ordinal];
        }
        return 0;
    }

    /**
     * Returns the function for this column or parameter when used in a query of the given type,
     * or {@code null} if none.
     */
    String getFunction(final QueryType type) {
        return (functions != null && type != null) ? functions.get(type) : null;
    }

    /**
     * Sets a function for this column or parameter when used in a query of the given type.
     */
    void setFunction(final String function, final QueryType... types) {
        if (functions == null) {
            functions = new EnumMap<QueryType,String>(QueryType.class);
        }
        for (final QueryType type : types) {
            functions.put(type, function);
        }
    }

    /**
     * Returns a hash code value for this language element.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(index);
    }

    /**
     * Compares this language element with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final IndexedSqlElement that = (IndexedSqlElement) object;
            return Arrays.equals(this.index, that.index);
        }
        return false;
    }
}
