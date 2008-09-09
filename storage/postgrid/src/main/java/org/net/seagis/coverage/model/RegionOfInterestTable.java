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
package net.seagis.coverage.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.catalog.Column;
import net.seagis.catalog.Parameter;
import static net.seagis.catalog.QueryType.*;


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
