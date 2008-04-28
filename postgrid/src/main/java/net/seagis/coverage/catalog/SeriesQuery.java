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
 * The query to execute for a {@link SeriesTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class SeriesQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, layer, pathname, extension, format, permission, remarks;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName, byLayer;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SeriesQuery(final Database database) {
        super(database, "Series");
        final QueryType[] SLF   = {SELECT, LIST, FILTERED_LIST};
        final QueryType[] SLFI  = {SELECT, LIST, FILTERED_LIST, INSERT};
        final QueryType[] SLFIE = {SELECT, LIST, FILTERED_LIST, INSERT, EXISTS};
        name       = addColumn("identifier",           SLFIE);
        layer      = addColumn("layer",                SLFI );
        pathname   = addColumn("pathname",             SLFI );
        extension  = addColumn("extension",            SLFI );
        format     = addColumn("format",               SLFI );
        permission = addColumn("permission", "public", SLF  );
        remarks    = addColumn("remarks",    null,     SLF  );
        byName     = addParameter(name,  SELECT, EXISTS);
        byLayer    = addParameter(layer, FILTERED_LIST);
        name.setOrdering("ASC", LIST, FILTERED_LIST);
    }
}
