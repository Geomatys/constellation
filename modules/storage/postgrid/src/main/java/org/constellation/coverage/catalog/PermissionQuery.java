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

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


/**
 * The query to execute for a {@link PermissionTable}.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 * @version $Id$
 */
final class PermissionQuery extends Query {
    /**
     * Default user. The value should match the default provided in
     * {@link org.constellation.catalog.ConfigurationKey#PERMISSION}.
     */
    static final String DEFAULT = "Anonymous";

    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column name, user, WMS, WCS, getInfo, description;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName, byUser;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public PermissionQuery(final Database database) {
        super(database, "Permissions");
        final QueryType[] SL = {SELECT, LIST};
        name        = addColumn("name",                  SL);
        user        = addColumn("user",    DEFAULT,      SL);
        WMS         = addColumn("WMS",     Boolean.TRUE, SL);
        WCS         = addColumn("WCS",     Boolean.TRUE, SL);
        getInfo     = addColumn("getInfo", Boolean.TRUE, SL);
        description = addColumn("description",           SL);
        byName      = addParameter(name, SL);
        byUser      = addParameter(user, SL);
    }
}
