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
    protected final Parameter byWidth, byHeight, byScaleX, byScaleY, byTranslateX, byTranslateY,
            byShearX, byShearY, byHorizontalSRID, byVerticalSRID;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public GridGeometryQuery(final Database database) {
        super(database);
        final QueryType[] FLSI = {FILTERED_LIST, LIST, SELECT, INSERT};
        final QueryType[] LSI  = {               LIST, SELECT, INSERT};
        final QueryType[] LS   = {               LIST, SELECT        };
        final QueryType[] F    = {FILTERED_LIST              };
        identifier        = addColumn("GridGeometries", "identifier",              FLSI);
        width             = addColumn("GridGeometries", "width",                    LSI);
        height            = addColumn("GridGeometries", "height",                   LSI);
        scaleX            = addColumn("GridGeometries", "scaleX",                   LSI);
        scaleY            = addColumn("GridGeometries", "scaleY",                   LSI);
        translateX        = addColumn("GridGeometries", "translateX",               LSI);
        translateY        = addColumn("GridGeometries", "translateY",               LSI);
        shearX            = addColumn("GridGeometries", "shearX",               0,  LSI);
        shearY            = addColumn("GridGeometries", "shearY",               0,  LSI);
        horizontalSRID    = addColumn("GridGeometries", "horizontalSRID",           LSI);
        horizontalExtent  = addColumn("GridGeometries", "horizontalExtent",         LS ); // Will rely on trigger for insertion.
        verticalSRID      = addColumn("GridGeometries", "verticalSRID",      null,  LSI);
        verticalOrdinates = addColumn("GridGeometries", "verticalOrdinates", null, FLSI);
        if (database.isSpatialEnabled()) {
            horizontalExtent.setFunction("Box2D", LS);
        }
        byIdentifier        = addParameter(identifier, SELECT);
        byWidth             = addParameter(width,          F);
        byHeight            = addParameter(height,         F);
        byScaleX            = addParameter(scaleX,         F);
        byScaleY            = addParameter(scaleY,         F);
        byTranslateX        = addParameter(translateX,     F);
        byTranslateY        = addParameter(translateY,     F);
        byShearX            = addParameter(shearX,         F);
        byShearY            = addParameter(shearY,         F);
        byHorizontalSRID    = addParameter(horizontalSRID, F);
        byVerticalSRID      = addParameter(verticalSRID,   F);
    }
}
