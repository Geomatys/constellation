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

import org.geotools.resources.Utilities;


/**
 * A reference from a {@linkplain #foreignerKey foreigner key} to a
 * {@linkplain #primaryKey primary key}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class CrossReference {
    /**
     * The column that reference a primary key.
     */
    public final Column foreignerKey;

    /**
     * The referenced primary key.
     */
    public final Column primaryKey;

    /**
     * Creates a new foreigner key.
     *
     * @param table The table name in which this column appears.
     * @param name  The column name.
     */
    public CrossReference(final Column foreignerKey, final Column primaryKey) {
        this.foreignerKey = foreignerKey;
        this.primaryKey   = primaryKey;
    }

    /**
     * Returns a hash code value for this cross reference.
     */
    @Override
    public int hashCode() {
        return foreignerKey.hashCode() + 31*primaryKey.hashCode();
    }

    /**
     * Compares the specified object with this cross reference for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof CrossReference) {
            final CrossReference that = (CrossReference) object;
            return Utilities.equals(this.foreignerKey, that.foreignerKey) &&
                   Utilities.equals(this.primaryKey,   that.primaryKey);
        }
        return false;
    }

    /**
     * Returns a string representation of this cross reference for debugging purpose.
     */
    @Override
    public String toString() {
        return foreignerKey.toString() + " \u2192 " + primaryKey.toString();
    }
}
