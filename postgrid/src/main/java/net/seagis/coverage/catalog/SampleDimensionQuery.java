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
 * The query to execute for a {@link SampleDimensionTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class SampleDimensionQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, band, units;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byFormat;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SampleDimensionQuery(final Database database) {
        super(database, "SampleDimensions");
        final Column format;
        final QueryType[] usage = {LIST, FILTERED_LIST};
        identifier = addColumn("identifier", usage);
        format     = addColumn("format",     LIST );
        band       = addColumn("band", 1,    usage);
        units      = addColumn("units",      usage);
        byFormat   = addParameter(format, FILTERED_LIST);
        band.setOrdering("ASC", usage);
    }
}
