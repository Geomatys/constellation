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

import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Query;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Role;
import static net.sicade.observation.sql.QueryType.*;


/**
 * The query to execute for a {@link ThematicTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class ThematicQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, remarks;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ThematicQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name    = addColumn   ("Thematics", "name",        usage);
        remarks = addColumn   ("Thematics", "description", usage);
        byName  = addParameter(name, SELECT);
        name.setRole(Role.NAME);
        name.setOrdering("ASC");
    }
}
