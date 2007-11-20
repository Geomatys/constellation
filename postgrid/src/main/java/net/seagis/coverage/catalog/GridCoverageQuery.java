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
        final QueryType[] ________ = {                                                                        };
        final QueryType[] SE____D_ = {SELECT, EXISTS,                                            DELETE       };
        final QueryType[] _E____DC = {        EXISTS,                                            DELETE, CLEAR};
        final QueryType[] S_L_____ = {SELECT,         LIST                                                    };
        final QueryType[] S_L__I__ = {SELECT,         LIST,                               INSERT              };
        final QueryType[] SEL__I__ = {SELECT, EXISTS, LIST,                               INSERT              };
        final QueryType[] S_LA_I__ = {SELECT,         LIST, AVAILABLE_DATA,               INSERT              };
        final QueryType[] S_LABI__ = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX, INSERT              };
        final QueryType[] S_LAB___ = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX                      };
        final QueryType[] SELAB_DC = {SELECT, EXISTS, LIST, AVAILABLE_DATA, BOUNDING_BOX,        DELETE, CLEAR};
        final QueryType[] ____B___ = {                                      BOUNDING_BOX                      };
        layer            = addForeignerColumn("Series",         "layer",            ________);
        series           = addColumn         (                  "series",           S_L__I__);
        filename         = addColumn         (                  "filename",         SEL__I__);
        index            = addColumn         (                  "index", 1,         S_L__I__);
        startTime        = addColumn         (                  "startTime",        S_LABI__);
        endTime          = addColumn         (                  "endTime",          S_LABI__);
        spatialExtent    = addColumn         (                  "extent",           S_LA_I__);
        horizontalExtent = addForeignerColumn("GridGeometries", "horizontalExtent", ____B___);
        visibility       = addForeignerColumn("Series",         "visible", true,    ________);

        startTime.setFunction("MIN",  ____B___);
        endTime  .setFunction("MAX",  ____B___);
        endTime  .setOrdering("DESC", S_L_____);
        series   .setOrdering("ASC",  S_L_____);

        byFilename         = addParameter(filename,           SE____D_);
        byLayer            = addParameter(layer,              S_LAB___);
        bySeries           = addParameter(series,             _E____DC);
        byStartTime        = addParameter(startTime,          SELAB_DC);
        byEndTime          = addParameter(endTime,            SELAB_DC);
        byHorizontalExtent = addParameter(horizontalExtent,   S_LAB___);
        byVisibility       = addParameter(visibility,         S_LAB___);
        if (database.isSpatialEnabled()) {
            byHorizontalExtent.setComparator("&&");
            byHorizontalExtent.setFunction("GeometryFromText(?,4326)", S_LAB___);
            horizontalExtent  .setFunction("EXTENT",                   ____B___);
        } else {
            throw new UnsupportedOperationException();
            // TODO: revisit.
        }
        byStartTime.setComparator("IS NULL OR <=");
        byEndTime  .setComparator("IS NULL OR >=");
    }
}
