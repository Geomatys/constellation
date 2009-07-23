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
 * The query to execute for a {@link GridCoverageTable}.
 * Entries <strong>must</strong> be sorted by date (either start or end time).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridCoverageQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column series, filename, index, startTime, endTime, spatialExtent;

    /**
     * For insertion of new entries in the {@code Tiles} table only.
     * Null otherwise.
     */
    final Column dx, dy;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byFilename, byLayer, bySeries,
            byStartTime, byEndTime, byHorizontalExtent;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     *
     * @todo Currently, we sort by end time first, then by series. In practice, there
     *       is no interrest in querying the full table instead of a table for a specific
     *       series, so we could swap the ordering, which would fit better the table index.
     *       It would also allow us to simplifying the Column implementation and remove the
     *       code that keep trace of "sorted by" order - we would put them in same order than
     *       columns, which again make the code simplier.
     */
    public GridCoverageQuery(final Database database) {
        this(database, false);
    }

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     * @param tiles {@code true} if this query is for the {@code "Tiles"} table.
     *        This is used for insertion of new entries only, not for reading.
     *        In the later case, {@code TileQuery} is used instead.
     */
    GridCoverageQuery(final Database database, final boolean tiles) {
        super(database, tiles ? "Tiles" : "GridCoverages", tiles);
        final Column layer, horizontalExtent;
        final QueryType[] empty   = {                                                                        };
        final QueryType[] sed     = {SELECT, EXISTS,                                            DELETE       };
        final QueryType[] edc     = {        EXISTS,                                            DELETE, CLEAR};
        final QueryType[] sl      = {SELECT,         LIST                                                    };
        final QueryType[] sli     = {SELECT,         LIST,                               INSERT              };
        final QueryType[] seli    = {SELECT, EXISTS, LIST,                               INSERT              };
        final QueryType[] slai    = {SELECT,         LIST, AVAILABLE_DATA,               INSERT              };
        final QueryType[] slabi   = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX, INSERT              };
        final QueryType[] slab    = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX                      };
        final QueryType[] selabdc = {SELECT, EXISTS, LIST, AVAILABLE_DATA, BOUNDING_BOX,        DELETE, CLEAR};
        final QueryType[] b       = {                                      BOUNDING_BOX                      };
        layer            = addForeignerColumn("Series",         "layer",            empty);
        series           = addColumn         (                  "series",           sli);
        filename         = addColumn         (                  "filename",         seli);
        index            = addColumn         (                  "index", 1,         sli);
        startTime        = addColumn         (                  "startTime",        slabi);
        endTime          = addColumn         (                  "endTime",          slabi);
        spatialExtent    = addColumn         (                  "extent",           slai);
        horizontalExtent = addForeignerColumn("GridGeometries", "horizontalExtent", b);
        if (tiles) {
            dx = addColumn("dx", INSERT);
            dy = addColumn("dy", INSERT);
        } else {
            dx = null;
            dy = null;
        }
        startTime.setFunction("MIN",  b);
        endTime  .setFunction("MAX",  b);
        endTime  .setOrdering("DESC", sl); // Sort by date is mandatory.
        series   .setOrdering("ASC",  sl);

        byFilename         = addParameter(filename,           sed);
        byLayer            = addParameter(layer,              slab);
        bySeries           = addParameter(series,             edc);
        byStartTime        = addParameter(startTime,          selabdc);
        byEndTime          = addParameter(endTime,            selabdc);
        byHorizontalExtent = addParameter(horizontalExtent,   slab);
        byHorizontalExtent.setComparator("&&");
        byHorizontalExtent.setFunction("GeometryFromText(?,4326)", slab);
        horizontalExtent  .setFunction("EXTENT",                   b);
        byStartTime.setComparator("IS NULL OR <=");
        byEndTime  .setComparator("IS NULL OR >=");
    }
}
