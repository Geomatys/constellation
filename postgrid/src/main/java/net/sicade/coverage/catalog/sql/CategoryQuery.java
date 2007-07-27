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
package net.sicade.coverage.catalog.sql;

import org.geotools.coverage.Category;
import net.sicade.sql.Database;
import net.sicade.sql.Column;
import net.sicade.sql.Parameter;
import net.sicade.sql.Query;
import net.sicade.sql.QueryType;
import static net.sicade.sql.QueryType.*;


/**
 * The query to execute for a {@link CategoryTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class CategoryQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, lower, upper, c0, c1, function, colors;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byBand;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public CategoryQuery(final Database database) {
        super(database);
        final Column band;
        final QueryType[] usage = {LIST, FILTERED_LIST};
        name     = addColumn   ("Categories", "name",     usage);
        band     = addColumn   ("Categories", "band",     LIST );
        lower    = addColumn   ("Categories", "lower",    usage);
        upper    = addColumn   ("Categories", "upper",    usage);
        c0       = addColumn   ("Categories", "c0",       usage);
        c1       = addColumn   ("Categories", "c1",       usage);
        function = addColumn   ("Categories", "function", usage);
        colors   = addColumn   ("Categories", "colors",   usage);
        byBand   = addParameter(band, FILTERED_LIST);
        lower.setOrdering("ASC");
    }
}
