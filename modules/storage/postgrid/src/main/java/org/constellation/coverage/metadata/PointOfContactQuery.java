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

import org.constellation.catalog.Database;
import org.constellation.catalog.Column;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


/**
 * The query to execute for a {@link PointOfContactTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class PointOfContactQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column pocId, lastName, firstName, address1, address2, city, state, country, 
            zip, phone, email, org, orgAddress1, orgAddress2, orgCity, orgState, orgZip,
            orgCountry, orgContact;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public PointOfContactQuery(final Database database) {
        super(database, "PointOfContact");
        final QueryType[] sl   = {SELECT, LIST};
        final QueryType[] slei = {SELECT, LIST, EXISTS, INSERT};
        pocId =            addColumn("poc_id",                   slei);
        lastName =         addColumn("last_name",        null,   sl  );
        firstName =        addColumn("first_name",       null,   sl  );
        address1 =         addColumn("address1",         null,   sl  );
        address2 =         addColumn("address2",         null,   sl  );
        city =             addColumn("city",             null,   sl  );
        state =            addColumn("state",            null,   sl  );
        country =          addColumn("country",          null,   sl  );
        zip =              addColumn("zip",              null,   sl  );
        phone =            addColumn("phone",            null,   sl  );
        email =            addColumn("email",            null,   sl  );
        org =              addColumn("org",              null,   sl  );
        orgAddress1 =      addColumn("org_address1",     null,   sl  );
        orgAddress2 =      addColumn("org_address2",     null,   sl  );
        orgCity =          addColumn("org_city",         null,   sl  );
        orgState =         addColumn("org_state",        null,   sl  );
        orgZip =           addColumn("org_zip",          null,   sl  );
        orgCountry =       addColumn("org_country",      null,   sl  );
        orgContact =       addColumn("org_contact",      null,   sl  );
        byName    = addParameter(pocId, SELECT, EXISTS);
    }
}
