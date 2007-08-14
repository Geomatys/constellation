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
package net.sicade.coverage.catalog;

import net.sicade.catalog.Database;
import net.sicade.catalog.Column;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;


/**
 * The query to execute for a {@link GridGeometryTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class GridGeometryQuery extends Query {
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Column identifier, width, height, scaleX, scaleY, translateX, translateY,
            shearX, shearY, horizontalSRID, horizontalExtent, verticalSRID, verticalOrdinates;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
//    protected final Parameter byWidth, byHeight, byDepth, byExtent;

    /**
     * The SQL instruction for inserting a new geographic bounding box.
     *
     * @todo Choose the CRS.
     */
//    private static final SpatialConfigurationKey INSERT = new SpatialConfigurationKey("GridGeometryes:INSERT",
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
    public GridGeometryQuery(final Database database) {
        super(database);
        final QueryType[] LS = {LIST, SELECT};
        identifier        = addColumn("GridGeometries", "identifier",        LS);
        width             = addColumn("GridGeometries", "width",             LS);
        height            = addColumn("GridGeometries", "height",            LS);
        scaleX            = addColumn("GridGeometries", "scaleX",            LS);
        scaleY            = addColumn("GridGeometries", "scaleY",            LS);
        translateX        = addColumn("GridGeometries", "translateX",        LS);
        translateY        = addColumn("GridGeometries", "translateY",        LS);
        shearX            = addColumn("GridGeometries", "shearX",            LS);
        shearY            = addColumn("GridGeometries", "shearY",            LS);
        horizontalSRID    = addColumn("GridGeometries", "horizontalSRID",    LS);
        horizontalExtent  = addColumn("GridGeometries", "horizontalExtent",  LS);
        verticalSRID      = addColumn("GridGeometries", "verticalSRID",      LS);
        verticalOrdinates = addColumn("GridGeometries", "verticalOrdinates", LS);
        if (database.isSpatialEnabled()) {
            horizontalExtent.setFunction("Box2D", LS);
        }
        byIdentifier = addParameter(identifier, SELECT);
//        byWidth      = addParameter(width,            SI);
//        byHeight     = addParameter(height,           SI);
//        byDepth      = addParameter(depth,            SI);
//        byExtent     = addParameter(horizontalExtent, SI);
    }
}
