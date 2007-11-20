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
        final QueryType[] _______ = {                                                                 };
        final QueryType[] SE____D = {SELECT, EXISTS,                                            DELETE};
        final QueryType[] _E____D = {        EXISTS,                                            DELETE};
        final QueryType[] S_L____ = {SELECT,         LIST                                             };
        final QueryType[] S_L__I_ = {SELECT,         LIST,                               INSERT       };
        final QueryType[] SEL__I_ = {SELECT, EXISTS, LIST,                               INSERT       };
        final QueryType[] S_LA_I_ = {SELECT,         LIST, AVAILABLE_DATA,               INSERT       };
        final QueryType[] S_LABI_ = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX, INSERT       };
        final QueryType[] S_LAB__ = {SELECT,         LIST, AVAILABLE_DATA, BOUNDING_BOX               };
        final QueryType[] SELAB_D = {SELECT, EXISTS, LIST, AVAILABLE_DATA, BOUNDING_BOX,        DELETE};
        final QueryType[] ____B__ = {                                      BOUNDING_BOX               };
        layer            = addForeignerColumn("Series",         "layer",            _______);
        series           = addColumn         (                  "series",           S_L__I_);
        filename         = addColumn         (                  "filename",         SEL__I_);
        index            = addColumn         (                  "index", 1,         S_L__I_);
        startTime        = addColumn         (                  "startTime",        S_LABI_);
        endTime          = addColumn         (                  "endTime",          S_LABI_);
        spatialExtent    = addColumn         (                  "extent",           S_LA_I_);
        horizontalExtent = addForeignerColumn("GridGeometries", "horizontalExtent", ____B__);
        visibility       = addForeignerColumn("Series",         "visible", true,    _______);

        startTime.setFunction("MIN",  ____B__);
        endTime  .setFunction("MAX",  ____B__);
        endTime  .setOrdering("DESC", S_L____);
        series   .setOrdering("ASC",  S_L____);

        byFilename         = addParameter(filename,           SE____D);
        byLayer            = addParameter(layer,              S_LAB__);
        bySeries           = addParameter(series,             _E____D);
        byStartTime        = addParameter(startTime,          SELAB_D);
        byEndTime          = addParameter(endTime,            SELAB_D);
        byHorizontalExtent = addParameter(horizontalExtent,   S_LAB__);
        byVisibility       = addParameter(visibility,         S_LAB__);
        if (database.isSpatialEnabled()) {
            byHorizontalExtent.setComparator("&&");
            byHorizontalExtent.setFunction("GeometryFromText(?,4326)", S_LAB__);
            horizontalExtent  .setFunction("EXTENT",                   ____B__);
        } else {
            throw new UnsupportedOperationException();
            // TODO: revisit.
        }
        byStartTime.setComparator("IS NULL OR <=");
        byEndTime  .setComparator("IS NULL OR >=");
    }
}
