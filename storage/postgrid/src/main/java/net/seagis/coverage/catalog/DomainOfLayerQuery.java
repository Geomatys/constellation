/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
 * The query to execute for a {@link DomainOfLayerTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class DomainOfLayerQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column layer, startTime, endTime, west, east, south, north, xResolution, yResolution;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byLayer;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DomainOfLayerQuery(final Database database) {
        super(database, "DomainOfLayers");
        final QueryType[] SL = {SELECT, LIST};
        layer       = addColumn("layer",       SL);
        startTime   = addColumn("startTime",   SL);
        endTime     = addColumn("endTime",     SL);
        west        = addColumn("west",        SL);
        east        = addColumn("east",        SL);
        south       = addColumn("south",       SL);
        north       = addColumn("north",       SL);
        xResolution = addColumn("xResolution", SL);
        yResolution = addColumn("yResolution", SL);
        byLayer     = addParameter(layer, SELECT);
    }
}
