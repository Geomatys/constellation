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

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


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
    protected final Column layer, series, pathname, filename, extension,
            startTime, endTime, spatialExtent, format;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byFilename, byLayer, byStartTime, byEndTime, byHorizontalExtent, byVisibility;

    /**
     * Creates a new query for the specified database.
     *
     * @param  database The database for which this query is created.
     */
    public GridCoverageQuery(final Database database) {
        super(database);
        final Column horizontalExtent, visibility;
        final QueryType[] hiden = {                            };
        final QueryType[] SL    = {SELECT, LIST                };
        final QueryType[] SLA   = {SELECT, LIST, AVAILABLE_DATA};
        final QueryType[]   A   = {              AVAILABLE_DATA};
        layer            = addColumn("Series",         "layer",            SL   );
        series           = addColumn("GridCoverages",  "series",           SL   );
        pathname         = addColumn("Series",         "pathname",         SL   );
        filename         = addColumn("GridCoverages",  "filename",         SL   );
        extension        = addColumn("Series",         "extension",        SL   );
        startTime        = addColumn("GridCoverages",  "startTime",        SLA  );
        endTime          = addColumn("GridCoverages",  "endTime",          SLA  );
        spatialExtent    = addColumn("GridCoverages",  "extent",           SLA  );
        horizontalExtent = addColumn("GridGeometries", "horizontalExtent", hiden);
        format           = addColumn("Series",         "format",           SL   );
        visibility       = addColumn("Series",         "visible",          hiden);

        endTime         .setOrdering("ASC");
//      series          .setOrdering("ASC"); // TODO: enable once declaration order is taken in account.
        byFilename         = addParameter(filename,      SELECT);
        byLayer            = addParameter(layer,            SLA);
        byStartTime        = addParameter(startTime,        SLA);
        byEndTime          = addParameter(endTime,          SLA);
        byHorizontalExtent = addParameter(horizontalExtent, SLA);
        byVisibility       = addParameter(visibility,       SLA);
        if (database.isSpatialEnabled()) {
            byHorizontalExtent.setComparator("&&");
            byHorizontalExtent.setFunction("GeometryFromText(?,4326)", SLA);
        } else {
            // TODO: revisit.
        }
        byStartTime.setComparator("IS NULL OR <=");
        byEndTime  .setComparator("IS NULL OR >=");
    }
}
