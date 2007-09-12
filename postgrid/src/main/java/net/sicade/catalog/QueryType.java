/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.catalog;

import java.util.logging.Level;


/**
 * The kind of query to be executed.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum QueryType {
    /**
     * Only one record will be selected using a name. This is the kind of query executed by
     * {@link SingletonTable#getEntry(String)}.
     */
    SELECT(),

    /**
     * Only one record will be selected using a numeric identifier. This is the kind of
     * query executed by {@link SingletonTable#getEntry(int)}.
     */
    SELECT_BY_IDENTIFIER(),

    /**
     * Every records will be listed. This is the kind of query executed by
     * {@link SingletonTable#getEntries()}.
     */
    LIST(),

    /**
     * Records will be listed using some filter. This is the same as {@link #LIST},
     * but with some additional criterions left to {@link Table} implementations.
     */
    FILTERED_LIST(),

    /**
     * Selects spatio-temporal envelope in a set of records. This is the kind of
     * query executed by {@link BoundedSingletonTable#getEnvelope()}.
     */
    BOUNDING_BOX(),

    /**
     * Selects a list of available dates or depths.
     */
    AVAILABLE_DATA(),

    /**
     * A record to be added in a table.
     */
    INSERT(LoggingLevel.INSERT);

    /**
     * The suggested level for logging SQL statement of this kind.
     */
    final Level level;

    /**
     * Creates a query type with the default {@link LoggingLevel#SELECT}.
     */
    private QueryType() {
        this(LoggingLevel.SELECT);
    }

    /**
     * Creates a query type.
     */
    private QueryType(final Level level) {
        this.level = level;
    }
}
