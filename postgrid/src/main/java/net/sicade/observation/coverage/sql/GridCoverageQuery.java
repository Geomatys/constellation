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
package net.sicade.observation.coverage.sql;

import java.sql.SQLException;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Query;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SpatialColumn;
import net.sicade.observation.sql.SpatialParameter;
import static net.sicade.observation.sql.QueryType.*;


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
            startTime, endTime, spatialExtent, width, height, depth, crs, format, visibility;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byFilename, byLayer, byStartTime, byEndTime, bySpatialExtent, byVisibility;

    /**
     * Creates a new query for the specified database.
     *
     * @throws SQLException if an error occured while reading the database.
     */
    public GridCoverageQuery(final Database database) throws SQLException {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        layer           = new Column   (this, "Series",         "layer",         usage);
        series          = new Column   (this, "GridCoverages",  "series",        usage);
        pathname        = new Column   (this, "Series",         "pathname",      usage);
        filename        = new Column   (this, "GridCoverages",  "filename",      usage);
        extension       = new Column   (this, "Series",         "extension",     usage);
        startTime       = new Column   (this, "GridCoverages",  "startTime",     usage);
        endTime         = new Column   (this, "GridCoverages",  "endTime",       usage);
        spatialExtent   = new SpatialColumn.Box(this, "GridGeometries", "spatialExtent", usage);
        width           = new Column   (this, "GridGeometries", "width",         usage);
        height          = new Column   (this, "GridGeometries", "height",        usage);
        depth           = new Column   (this, "GridGeometries", "depth",         usage);
        crs             = new Column   (this, "GridGeometries", "CRS",           usage);
        format          = new Column   (this, "Series",         "format",        usage);
        visibility      = new Column   (this, "Series",         "visible",       usage);
        byFilename      = new Parameter(this, filename,      SELECT);
        byLayer         = new Parameter(this, layer,         usage);
        byStartTime     = new Parameter(this, startTime,     usage);
        byEndTime       = new Parameter(this, endTime,       usage);
        bySpatialExtent = new SpatialParameter.Box(this, spatialExtent, usage);
        byVisibility    = new Parameter(this, visibility,    usage);
        if (SpatialColumn.WORKAROUND_POSTGIS) {
            // PostGIS doesn't seem to be able to apply conversions by itself.
            bySpatialExtent.setFunction("::text", usage);
        }
        filename       .setRole(Role.NAME);
        spatialExtent  .setRole(Role.SPATIAL_ENVELOPE);
        startTime      .setRole(Role.TIME_RANGE);
        endTime        .setRole(Role.TIME_RANGE);
        endTime        .setOrdering("ASC");
//      series         .setOrdering("ASC"); // TODO: enable once declaration order is taken in account.
        byStartTime    .setComparator("IS NULL OR <=");
        byEndTime      .setComparator("IS NULL OR >=");
        bySpatialExtent.setComparator("&&");
    }
}
