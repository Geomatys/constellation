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
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Query;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.SpatialColumn;
import net.sicade.observation.sql.SpatialParameter;
import static net.sicade.observation.sql.QueryType.*;


/**
 * The query to execute for a {@link CategoryTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class GeographicBoundingBoxQuery extends Query {
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final SpatialParameter.Box byExtent;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byWidth, byHeight, byDepth;

    /**
     * The SQL instruction for inserting a new geographic bounding box.
     *
     * @todo Choose the CRS.
     */
//    private static final SpatialConfigurationKey INSERT = new SpatialConfigurationKey("GeographicBoundingBoxes:INSERT",
//            "INSERT INTO \"GridGeometries\"\n" +
//            "  (id, \"westBoundLongitude\",\n" +
//            "       \"eastBoundLongitude\",\n" +
//            "       \"southBoundLatitude\",\n" +
//            "       \"northBoundLatitude\",\n" +
//            "       \"altitudeMin\",\n"        +
//            "       \"altitudeMax\",\n"        +
//            "       \"CRS\",\n"                +
//            "       width, height, depth)\n"   +
//            "  VALUES (?, ?, ?, ?, ?, ?, ?, 'IRD:WGS84(xyt)', ?, ?, ?)",
//
//            "INSERT INTO coverages.\"GridGeometries\"\n"+
//            "  (id, \"spatialExtent\",\n"               +
//            "       \"CRS\",\n"                         +
//            "       width, height, depth)\n"            +
//            "  VALUES (?, ?, 'IRD:WGS84(xyt)', ?, ?, ?)");

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public GeographicBoundingBoxQuery(final Database database) throws SQLException {
        super(database);
        final Column name, width, height, depth;
        final SpatialColumn.Box spatialExtent;
        final QueryType[] usageLW = {LIST,   INSERT};
        final QueryType[] usageRW = {SELECT, INSERT};
        name          = addColumn   ("GridGeometries", "id",     usageRW);
        spatialExtent = new SpatialColumn.Box(this, "GridGeometries", "spatialExtent", usageLW);
        width         = addColumn   ("GridGeometries", "width",  usageLW);
        height        = addColumn   ("GridGeometries", "height", usageLW);
        depth         = addColumn   ("GridGeometries", "depth",  usageLW);
        byExtent      = new SpatialParameter.Box(this, spatialExtent, usageRW);
        byWidth       = addParameter(width,                      usageRW);
        byHeight      = addParameter(height,                     usageRW);
        byDepth       = addParameter(depth,                      usageRW);
        name.setRole(Role.NAME);
    }
}
