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

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


/**
 * The query to execute for a {@link GridCoverageTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridCoverageQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column series, filename, index, startTime, endTime, spatialExtent;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byFilename, byLayer, bySeries,
            byStartTime, byEndTime, byHorizontalExtent, byVisibility;

    /**
     * Creates a new query for the specified database.
     *
     * @param  database The database for which this query is created.
     */
    public GridCoverageQuery(final Database database) {
        super(database, "GridCoverages");
        final Column layer, horizontalExtent, visibility;
        final QueryType[] hiden = {                                                  };
        final QueryType[] SL    = {SELECT, LIST                                      };
        final QueryType[] SLI   = {SELECT, LIST,                               INSERT};
        final QueryType[] SLAI  = {SELECT, LIST, AVAILABLE_DATA,               INSERT};
        final QueryType[] SLABI = {SELECT, LIST, AVAILABLE_DATA, BOUNDING_BOX, INSERT};
        final QueryType[] SLAB  = {SELECT, LIST, AVAILABLE_DATA, BOUNDING_BOX        };
        final QueryType[]    B  = {                              BOUNDING_BOX        };
        layer            = addForeignerColumn("Series", "layer", hiden);
        series           = addColumn("series",    SLI  );
        filename         = addColumn("filename",  SLI  );
        index            = addColumn("index", 1,  SLI  );
        startTime        = addColumn("startTime", SLABI);
        endTime          = addColumn("endTime",   SLABI);
        spatialExtent    = addColumn("extent",    SLAI );
        horizontalExtent = addForeignerColumn("GridGeometries", "horizontalExtent", B);
        visibility       = addForeignerColumn("Series", "visible", true, hiden);

        startTime.setFunction("MIN", B);
        endTime  .setFunction("MAX", B);
        endTime  .setOrdering("DESC", SL);
        series   .setOrdering("ASC",  SL);

        byFilename         = addParameter(filename,       SELECT);
        byLayer            = addParameter(layer,            SLAB);
        bySeries           = addParameter(series,         EXISTS);
        byStartTime        = addParameter(startTime,        SLAB);
        byEndTime          = addParameter(endTime,          SLAB);
        byHorizontalExtent = addParameter(horizontalExtent, SLAB);
        byVisibility       = addParameter(visibility,       SLAB);
        if (database.isSpatialEnabled()) {
            byHorizontalExtent.setComparator("&&");
            byHorizontalExtent.setFunction("GeometryFromText(?,4326)", SLAB);
            horizontalExtent  .setFunction("EXTENT", B);
        } else {
            throw new UnsupportedOperationException();
            // TODO: revisit.
        }
        byStartTime.setComparator("IS NULL OR <=");
        byEndTime  .setComparator("IS NULL OR >=");
    }
}
