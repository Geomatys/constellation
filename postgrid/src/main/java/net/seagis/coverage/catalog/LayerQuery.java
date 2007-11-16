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
 * The query to execute for a {@link CategoryTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class LayerQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, thematic, period, fallback, remarks;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

//    private static final SpatialConfigurationKey LIST = new SpatialConfigurationKey("Layer:LIST",
//            "SELECT name, phenomenon, procedure, period, fallback, description\n"      +
//            "  FROM \"Layers\" "                                                       +
//            "  JOIN (\n"                                                               +
//            "   SELECT DISTINCT layer, visible FROM \"Series\"\n"                      +
//            "   JOIN \"GridCoverages\""         + " ON series=\"Series\".identifier\n" +
//            "   JOIN \"GridGeometries\""        + " ON extent=\"GridGeometries\".id\n" +
//            "   WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                 +
//            "     AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                 +
//            "     AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"     +
//            "     AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"     +
//            "     AND (\"altitudeMax\"       >=? AND \"altitudeMin\"<=?)\n"            +
//            "  ) "                                                                     +
//            "  AS \"Selected\" ON layer=\"Layers\".name\n"                             +
//            "  WHERE visible=TRUE\n"                                                   +
//            "  ORDER BY name",
//
//            "SELECT name, phenomenon, procedure, period, fallback, description\n"      +
//            "  FROM \"Layers\" "                                                       +
//            "  JOIN (\n"                                                               +
//            "   SELECT DISTINCT layer, visible FROM \"Series\"\n"                      +
//            "   JOIN \"GridCoverages\""         + " ON series=\"Series\".identifier\n" +
//            "   JOIN \"GridGeometries\""        + " ON extent=\"GridGeometries\".id\n" +
//            "   WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                 +
//            "     AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                 +
//            "     AND (\"spatialExtent\" && ?)\n"     +
//            "  ) "                                                                     +
//            "  AS \"Selected\" ON layer=\"Layers\".name\n"                             +
//            "  WHERE visible=TRUE\n"                                                   +
//            "  ORDER BY name");

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public LayerQuery(final Database database) {
        super(database, "Layers");
        final QueryType[] SL = {SELECT, LIST};
        name      = addColumn("name",              SL);
        thematic  = addColumn("thematic",    null, SL);
        period    = addColumn("period",         1, SL);
        fallback  = addColumn("fallback",    null, SL);
        remarks   = addColumn("description", null, SL);
        byName    = addParameter(name, SELECT);
    }
}
