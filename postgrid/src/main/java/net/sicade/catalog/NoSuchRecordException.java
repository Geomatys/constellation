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

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.resources.i18n.ResourceKeys;
import net.sicade.resources.i18n.Resources;


/**
 * Throws when a record was not found for the specified key.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NoSuchRecordException extends CatalogException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3105861955682823122L;

    /**
     * Creates an exception from the specified result set. The table and column names are
     * obtained from the {@code results} argument if non-null. <strong>Note that the result
     * set will be closed</strong>, because this exception is always thrown when an error
     * occured while reading this result set.
     *
     * @param results The result set used in order to look for a record.
     * @param column  The column index of the primary key (first column index is 1).
     * @param key     The key value for the record that was not found.
     * @throws SQLException if the metadata can't be read from the result set.
     */
    public NoSuchRecordException(final ResultSet results, final int column, final String key) throws SQLException {
        setMetadata(results, column, key);
    }

    /**
     * Returns a localized message created from the information provided at construction time.
     */
    @Override
    public String getLocalizedMessage() {
        return Resources.format(ResourceKeys.ERROR_KEY_NOT_FOUND_$2, getTableName(), getPrimaryKey());
    }
}
