/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import org.constellation.catalog.Database;
import org.constellation.catalog.Column;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


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
    final Column name, layer, pathname, extension, format, permission, remarks;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName, byLayer;

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
        permission = addColumn("permission", "Public", SLF  );
        remarks    = addColumn("remarks",    null,     SLF  );
        byName     = addParameter(name,  SELECT, EXISTS);
        byLayer    = addParameter(layer, FILTERED_LIST);
        name.setOrdering("ASC", LIST, FILTERED_LIST);
    }
}
