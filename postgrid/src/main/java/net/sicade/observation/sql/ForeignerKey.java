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
package net.sicade.observation.sql;

import org.geotools.resources.Utilities;


/**
 * A column in a SQL {@linkplain Query query} which is a reference to a primary key.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ForeignerKey extends Column {
    /**
     * The referenced primary key.
     */
    final Column primaryKey;

    /**
     * Creates a new foreigner key.
     *
     * @param table The table name in which this column appears.
     * @param name  The column name.
     */
    public ForeignerKey(final Query query, final String table, final String name, final Column primaryKey) {
        super(query, table, name);
        this.primaryKey = primaryKey;
    }

    /**
     * Compares this column with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final ForeignerKey that = (ForeignerKey) object;
            return Utilities.equals(primaryKey, that.primaryKey);
        }
        return false;
    }
}
