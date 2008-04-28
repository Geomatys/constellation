/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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
package net.seagis.coverage.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.SingletonTable;


/**
 * Connection to a table of {@linkplain PermissionEntry permissions}.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @version $Id: CategoryTable.java 455 2008-03-13 15:21:44Z desruisseaux $
 */
final class PermissionTable extends SingletonTable<PermissionEntry> {
    /**
     * Creates a new table for the given database.
     */
    public PermissionTable(final Database database) {
        this(new PermissionQuery(database));
    }

    /**
     * Creates a permission table using the given query.
     */
    private PermissionTable(final PermissionQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a new entry for the current row in the given result set.
     */
    @Override
    protected PermissionEntry createEntry(final ResultSet results)
            throws CatalogException, SQLException
    {
        final PermissionQuery query = (PermissionQuery) super.query;
        return new PermissionEntry(results.getString (indexOf(query.name)),
                                   results.getString (indexOf(query.include)),
                                   results.getBoolean(indexOf(query.WCS)),
                                   results.getBoolean(indexOf(query.WMS)),
                                   results.getString (indexOf(query.description)));
    }

    /**
     * Completes the {@link PermissionEntry} creation after the {@link ResultSet} has been closed.
     */
    @Override
    protected void postCreateEntry(final PermissionEntry entry)
            throws CatalogException, SQLException
    {
        entry.postCreateEntry(this);
    }
}
