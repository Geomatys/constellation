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
 *
 * @author guilhem
 */
final class PermissionTable extends SingletonTable<PermissionEntry> {
    /**
     * Construit une table des permissions.
     *
     * @param  database Connexion vers la base de données.
     */
    public PermissionTable(final Database database) {
        this(new PermissionQuery(database));
    }

    /**
     * Construit une nouvelle table non partagée
     */
    public PermissionTable(final PermissionTable table) {
        super(table);
    }

    /**
     *
     */
    private PermissionTable(final PermissionQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    @Override
    protected PermissionEntry createEntry(ResultSet results) throws CatalogException, SQLException {
        final PermissionQuery query = (PermissionQuery) super.query;
        return new PermissionEntry(results.getString (indexOf(query.name)),
                                   results.getString (indexOf(query.include)),
                                   results.getBoolean(indexOf(query.WCS)),
                                   results.getBoolean(indexOf(query.WMS)),
                                   results.getString (indexOf(query.description)));
    }

    @Override
    protected void postCreateEntry(PermissionEntry entry)
            throws CatalogException, SQLException
    {
        entry.postCreateEntry(this);
    }
}
