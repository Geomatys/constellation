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
package net.sicade.coverage.model;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


/**
 * The query to execute for a {@link DistributionTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class DistributionQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, scale, offset, log;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DistributionQuery(final Database database) {
        super(database);
        final QueryType[] usage  = {SELECT, LIST};
        name   = addColumn   ("Distributions", "name",   usage);
        scale  = addColumn   ("Distributions", "scale",  usage);
        offset = addColumn   ("Distributions", "offset", usage);
        log    = addColumn   ("Distributions", "log",    usage);
        byName = addParameter(name, SELECT);
    }
}
