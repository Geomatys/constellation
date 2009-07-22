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
            zip, phone, email, org, org_address1, org_address2, org_city, org_state, org_zip, 
            org_country, org_contact;

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
        final QueryType[] SL   = {SELECT, LIST};
        final QueryType[] SLEI = {SELECT, LIST, EXISTS, INSERT};
        pocId =            addColumn("poc_id",                   SLEI);
        lastName =         addColumn("last_name",        null,   SL  );
        firstName =        addColumn("first_name",       null,   SL  );
        address1 =         addColumn("address1",         null,   SL  );
        address2 =         addColumn("address2",         null,   SL  );
        city =             addColumn("city",             null,   SL  );
        state =            addColumn("state",            null,   SL  );
        country =          addColumn("country",          null,   SL  );
        zip =              addColumn("zip",              null,   SL  );
        phone =            addColumn("phone",            null,   SL  );
        email =            addColumn("email",            null,   SL  );
        org =              addColumn("org",              null,   SL  );
        org_address1 =     addColumn("org_address1",     null,   SL  );
        org_address2 =     addColumn("org_address2",     null,   SL  );
        org_city =         addColumn("org_city",         null,   SL  );
        org_state =        addColumn("org_state",        null,   SL  );
        org_zip =          addColumn("org_zip",          null,   SL  );
        org_country =      addColumn("org_country",      null,   SL  );
        org_contact =      addColumn("org_contact",      null,   SL  );
        byName    = addParameter(pocId, SELECT, EXISTS);
    }
}
