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
package org.constellation.coverage.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;

import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;


/**
 * Connection to the table of {@linkplain Distribution distributions}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class DistributionTable extends SingletonTable<Distribution> {
    /**
     * Creates a distribution table.
     *
     * @param database Connection to the database.
     */
    public DistributionTable(final Database database) {
        super(new DistributionQuery(database));
        setIdentifierParameters(((DistributionQuery) query).byName, null);
    }

    /**
     * Creates a distribution entry from the current row in the specified result set.
     *
     * @throws SQLException if an error occured while reading the database.
     */
    @Override
    protected Distribution createEntry(final ResultSet results) throws CatalogException, SQLException {
        final DistributionQuery query = (DistributionQuery) super.query;
        return new DistributionEntry(results.getString (indexOf(query.name  )),
                                     results.getDouble (indexOf(query.scale )),
                                     results.getDouble (indexOf(query.offset)),
                                     results.getBoolean(indexOf(query.log   )));
    }

    /**
     * If the given distribution is presents in the database, return its identifier. Otherwise adds
     * a new entry in the table and returns its identifier.
     *
     * @param distribution The distribution to search for.
     *
     * @todo Current implementation requires a {@code DistributionEntry} implementation.
     *
     * @todo Current implementation is not really what we expect. We want to search for an entry
     *       having the same scale, offset, etc. Not an entry having the same name.
     */
    public synchronized String getIdentifier(final Distribution distribution)
            throws SQLException, CatalogException
    {
        final DistributionQuery query  = (DistributionQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            final String name = distribution.getName();
            if (name != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.name), name);
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    success = true;
                    return distribution.getName();
                } else {
                    id = distribution.getName();
                }
            } else {
                id = searchFreeIdentifier("distribution");
            }
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            final DistributionEntry entry = (DistributionEntry) distribution;
            statement.setString(indexOf(query.name),    id);
            statement.setBoolean(indexOf(query.log),    entry.isLog());
            statement.setDouble(indexOf(query.offset) , entry.getOffset());
            statement.setDouble(indexOf(query.scale),   entry.getScale());
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
