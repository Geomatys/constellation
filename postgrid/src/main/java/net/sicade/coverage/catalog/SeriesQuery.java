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
package net.sicade.coverage.catalog;

import net.sicade.catalog.Database;
import net.sicade.catalog.Column;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


/**
 * The query to execute for a {@link SeriesTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class SeriesQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, format;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName, byLayer;

    /**
     * A shared series table. For internal usage by {@link SeriesTable#getShared} only.
     */
    transient SeriesTable sharedTable;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SeriesQuery(final Database database) {
        super(database);
        final Column layer;
        final QueryType[] usage = {SELECT, LIST, FILTERED_LIST};
        name    = addColumn("Series", "identifier", usage);
        layer   = addColumn("Series", "layer",      usage);
        format  = addColumn("Series", "format",     usage);
        byName  = addParameter(name,  SELECT);
        byLayer = addParameter(layer, FILTERED_LIST);
        name.setOrdering("ASC");
    }
}
