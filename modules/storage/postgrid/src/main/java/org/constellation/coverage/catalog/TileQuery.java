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

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


/**
 * The query to execute for a {@link TileTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class TileQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column series, filename, index, spatialExtent, dx, dy;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byLayer, byStartTime, byEndTime, byHorizontalSRID;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public TileQuery(final Database database) {
        super(database, "Tiles");
        final Column layer, startTime, endTime, horizontalSRID;
        final QueryType[] none = {    };
        final QueryType[] list = {LIST};
        layer          = addForeignerColumn("Series",         "layer",          none);
        series         = addColumn         (                  "series",         list);
        filename       = addColumn         (                  "filename",       list);
        index          = addColumn         (                  "index", 1,       list);
        startTime      = addColumn         (                  "startTime",      none);
        endTime        = addColumn         (                  "endTime",        none);
        spatialExtent  = addColumn         (                  "extent",         list);
        dx             = addColumn         (                  "dx", 0,          list);
        dy             = addColumn         (                  "dy", 0,          list);
        horizontalSRID = addForeignerColumn("GridGeometries", "horizontalSRID", none);

        byLayer          = addParameter(layer,          list);
        byStartTime      = addParameter(startTime,      list);
        byEndTime        = addParameter(endTime,        list);
        byHorizontalSRID = addParameter(horizontalSRID, list);
        /*
         * Following conditions are the opposite of GridCoverageQuery because we wants
         * every tiles included in the range of the coverage, not tiles intercepting.
         */
        byStartTime.setComparator("IS NULL OR >=");
        byEndTime  .setComparator("IS NULL OR <=");
    }
}
