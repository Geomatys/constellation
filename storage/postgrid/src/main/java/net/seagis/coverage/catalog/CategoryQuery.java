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
package net.seagis.coverage.catalog;

import net.seagis.catalog.Database;
import net.seagis.catalog.Column;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


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
        super(database, "Categories");
        final Column band;
        final QueryType[] none = {    };
        final QueryType[] list = {LIST};
        name     = addColumn("name",           list);
        band     = addColumn("band",           none);
        lower    = addColumn("lower",          list);
        upper    = addColumn("upper",          list);
        c0       = addColumn("c0",             list);
        c1       = addColumn("c1",             list);
        function = addColumn("function", null, list);
        colors   = addColumn("colors",         list);
        byBand   = addParameter(band, list);
        lower.setOrdering("ASC", list);
    }
}
