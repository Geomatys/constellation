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
            startTime, endTime, spatialExtent, xmin, xmax, ymin, ymax, zmin, zmax,
            width, height, depth, crs, format, visibility;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byFilename, byLayer, byStartTime, byEndTime, bySpatialExtent, byVisibility;

    /**
     * Creates a new query for the specified database.
     *
     * @param  database The database for which this query is created.
     * @throws SQLException if an error occured while reading the database.
     */
    public GridCoverageQuery(final Database database) throws SQLException {
        super(database);
        final QueryType[] SL  = {SELECT, LIST                 };
        final QueryType[] SLA = {SELECT, LIST, AVAILABLE_DATA};
        final QueryType[]   A = {              AVAILABLE_DATA};
        layer           = addColumn("Series",         "layer",     SL );
        series          = addColumn("GridCoverages",  "series",    SL );
        pathname        = addColumn("Series",         "pathname",  SL );
        filename        = addColumn("GridCoverages",  "filename",  SL );
        extension       = addColumn("Series",         "extension", SL );
        startTime       = addColumn("GridCoverages",  "startTime", SLA);
        endTime         = addColumn("GridCoverages",  "endTime",   SLA);
        spatialExtent   = new SpatialColumn.Box(this, "GridGeometries", "spatialExtent", SL);
        xmin            = addColumn("GridGeometries", "spatialExtent", "xmin", A);
        xmax            = addColumn("GridGeometries", "spatialExtent", "xmax", A);
        ymin            = addColumn("GridGeometries", "spatialExtent", "ymin", A);
        ymax            = addColumn("GridGeometries", "spatialExtent", "ymax", A);
        zmin            = addColumn("GridGeometries", "spatialExtent", "zmin", A);
        zmax            = addColumn("GridGeometries", "spatialExtent", "zmax", A);
        width           = addColumn("GridGeometries", "width",     SL );
        height          = addColumn("GridGeometries", "height",    SL );
        depth           = addColumn("GridGeometries", "depth",     SL );
        crs             = addColumn("GridGeometries", "CRS",       SL );
        format          = addColumn("Series",         "format",    SL );
        visibility      = addColumn("Series",         "visible",   SLA);
        byFilename      = addParameter(filename,   SELECT);
        byLayer         = addParameter(layer,      SLA);
        byStartTime     = addParameter(startTime,  SLA);
        byEndTime       = addParameter(endTime,    SLA);
        bySpatialExtent = new SpatialParameter.Box(this, spatialExtent, SLA);
        byVisibility    = addParameter(visibility, SLA);
        if (SpatialColumn.WORKAROUND_POSTGIS) {
            // PostGIS doesn't seem to be able to apply conversions by itself.
            bySpatialExtent.setFunction("::text", SLA);
        }
        xmin           .setFunction("xmin", A);
        xmax           .setFunction("xmax", A);
        ymin           .setFunction("ymin", A);
        ymax           .setFunction("ymax", A);
        zmin           .setFunction("zmin", A);
        zmax           .setFunction("zmax", A);
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
