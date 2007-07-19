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
import static net.sicade.observation.sql.QueryType.*;


/**
 * The query to execute for a {@link LinearModelTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class LinearModelQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column source1, source2, coefficient;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byTarget;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public LinearModelQuery(final Database database) {
        super(database);
        final Column target;
        final QueryType[] usage = {SELECT};
        target      = addColumn   ("LinearModelTerms", "target",      LIST);
        source1     = addColumn   ("LinearModelTerms", "source1",     usage);
        source2     = addColumn   ("LinearModelTerms", "source2",     usage);
        coefficient = addColumn   ("LinearModelTerms", "coefficient", usage);
        byTarget    = addParameter(target, usage);
        source1.setOrdering("ASC");
        source2.setOrdering("ASC");
    }
}
