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
package org.constellation.coverage.catalog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotools.util.Utilities;


/**
 * Connection to a table of {@linkplain PermissionEntry permissions}.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 * @version $Id$
 */
final class PermissionTable extends SingletonTable<PermissionEntry> {
    /**
     * The user connected to the database. This is the user declared in the {@code config.xml}
     * file. It may or may not be the same one than the user given to the JDBC driver.
     */
    private String user = PermissionQuery.DEFAULT;

    /**
     * Creates a new table for the given database.
     */
    public PermissionTable(final Database database) {
        this(new PermissionQuery(database));
    }

     /**
     * Creates a shared table.
     */
    public PermissionTable(final PermissionTable table) {
        super(table);
    }

    /**
     * Creates a permission table using the given query.
     */
    private PermissionTable(final PermissionQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Returns the current user.
     *
     * @return The current user.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user The new user.
     */
    public void setUser(final String user) {
        ensureNonNull("user", user);
        if (!Utilities.equals(this.user, user)) {
            this.user = user;
            fireStateChanged("user");
        }
    }

    /**
     * Sets the user parameter in the given statement.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement)
            throws SQLException, CatalogException
    {
        super.configure(type, statement);
        final PermissionQuery query = (PermissionQuery) super.query;
        statement.setString(indexOf(query.byUser), user);
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
                                   results.getString (indexOf(query.user)),
                                   results.getBoolean(indexOf(query.WCS)),
                                   results.getBoolean(indexOf(query.WMS)),
                                   results.getBoolean(indexOf(query.getInfo)),
                                   results.getString (indexOf(query.description)));
    }
}
