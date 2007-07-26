/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.RegionOfInterest;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connection to the table of {@linkplain RegionOfInterest spatio-temporal offsets} relative
 * to the position of observations.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class RegionOfInterestTable extends SingletonTable<RegionOfInterest> {
    /**
     * Creates a location offset table.
     * 
     * @param database Connection to the database.
     */
    public RegionOfInterestTable(final Database database) {
        super(new RegionOfInterestQuery(database));
        setIdentifierParameters(((RegionOfInterestQuery) query).byName, null);
    }

    /**
     * Creates an entry from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected RegionOfInterest createEntry(final ResultSet results) throws SQLException, CatalogException {
        final RegionOfInterestQuery query = (RegionOfInterestQuery) super.query;
        return new RegionOfInterestEntry(
                results.getString(indexOf(query.name)),
                results.getDouble(indexOf(query.dx  )),
                results.getDouble(indexOf(query.dy  )),
                results.getDouble(indexOf(query.dz  )),
                Math.round(results.getDouble(indexOf(query.dt)) * RegionOfInterestEntry.DAY));
    }
}
