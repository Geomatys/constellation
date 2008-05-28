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

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


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
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, user, WMS, WCS, description;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName, byUser;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public PermissionQuery(final Database database) {
        super(database, "Permissions");
        final QueryType[] SL = {SELECT, LIST};
        name        = addColumn("name",                  SL);
        user        = addColumn("user",    "Anonymous",  SL);
        WMS         = addColumn("WMS",     Boolean.TRUE, SL);
        WCS         = addColumn("WCS",     Boolean.TRUE, SL);
        description = addColumn("description",           SL);
        byName      = addParameter(name, SL);
        byUser      = addParameter(user, SL);
    }
}
