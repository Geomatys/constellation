/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
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
package org.constellation.coverage.metadata;

import org.constellation.coverage.catalog.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.constellation.catalog.Database;
import org.constellation.catalog.BoundedSingletonTable;
import org.constellation.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain PointOfContact layer-level metadata}.
 * 
 * Metadata specific to the {@linkplain Layer layer}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
public class PointOfContactTable extends BoundedSingletonTable<PointOfContact> {
    
    /**
     * Creates a layer metadata table.
     *
     * @param database Connection to the database.
     */
    public PointOfContactTable(final Database database) {
        this(new PointOfContactQuery(database));
    }

    /**
     * Constructs a new {@code LayerTable} from the specified query.
     */
    private PointOfContactTable(final PointOfContactQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a layer metadata table using the same initial configuration than the specified table.
     */
    public PointOfContactTable(final PointOfContactTable table) {
        super(table);
    }

    /**
     * Creates a layer metadata from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected PointOfContact createEntry(final ResultSet results) throws CatalogException, SQLException {
        final PointOfContactQuery query = (PointOfContactQuery) super.query;
        final String pocId =            results.getString(indexOf(query.pocId));
        final String lastName =         results.getString(indexOf(query.lastName));
        final String firstName =        results.getString(indexOf(query.firstName));
        final String address1 =         results.getString(indexOf(query.address1));
        final String address2 =         results.getString(indexOf(query.address2));
        final String city =             results.getString(indexOf(query.city));
        final String state =            results.getString(indexOf(query.state));
        final String country =          results.getString(indexOf(query.country));
        final String zip =              results.getString(indexOf(query.zip));
        final String phone =            results.getString(indexOf(query.phone));
        final String email =            results.getString(indexOf(query.email));
        final String org =              results.getString(indexOf(query.org));
        final String orgAddress1 =      results.getString(indexOf(query.orgAddress1));
        final String orgAddress2 =      results.getString(indexOf(query.orgAddress2));
        final String orgCity =          results.getString(indexOf(query.orgCity));
        final String orgState =         results.getString(indexOf(query.orgState));
        final String orgZip =           results.getString(indexOf(query.orgZip));
        final String orgCountry =       results.getString(indexOf(query.orgCountry));
        final String orgContact =       results.getString(indexOf(query.orgContact));
        final PointOfContactEntry entry = new PointOfContactEntry(pocId, lastName, firstName, 
                address1, address2, city, state, country, zip, phone, email, org, orgAddress1,
                orgAddress2, orgCity, orgState, orgZip, orgCountry, orgContact);
        return entry;
    }

    /**
     * Clears this table cache.
     */
    @Override
    public synchronized void flush() {
        super.flush();
    }
}
