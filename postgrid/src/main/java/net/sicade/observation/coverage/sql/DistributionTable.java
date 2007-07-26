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
package net.sicade.observation.coverage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.observation.CatalogException;

import net.sicade.observation.Distribution;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.SingletonTable;


/**
 * Connection to the table of {@linkplain Distribution distributions}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
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
    protected Distribution createEntry(final ResultSet results) throws CatalogException, SQLException {
        final DistributionQuery query = (DistributionQuery) super.query;
        return new DistributionEntry(results.getString (indexOf(query.name  )),
                                     results.getDouble (indexOf(query.scale )),
                                     results.getDouble (indexOf(query.offset)),
                                     results.getBoolean(indexOf(query.log   )));
    }
}
