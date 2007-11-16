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
 * The query to execute for a {@link GridGeometryTable}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class GridGeometryQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, width, height, scaleX, scaleY, translateX, translateY,
            shearX, shearY, horizontalSRID, horizontalExtent, verticalSRID, verticalOrdinates;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier, byWidth, byHeight, byScaleX, byScaleY,
            byTranslateX, byTranslateY, byShearX, byShearY, byHorizontalSRID;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public GridGeometryQuery(final Database database) {
        super(database, "GridGeometries");
        final QueryType[] LFSEI = {LIST, FILTERED_LIST, SELECT, EXISTS, INSERT};
        final QueryType[] LFSI  = {LIST, FILTERED_LIST, SELECT,         INSERT};
        final QueryType[] LSI   = {LIST,                SELECT,         INSERT};
        final QueryType[] LS    = {LIST,                SELECT                };
        identifier        = addColumn("identifier",             LFSEI);
        width             = addColumn("width",                    LSI);
        height            = addColumn("height",                   LSI);
        scaleX            = addColumn("scaleX",                   LSI);
        scaleY            = addColumn("scaleY",                   LSI);
        translateX        = addColumn("translateX",               LSI);
        translateY        = addColumn("translateY",               LSI);
        shearX            = addColumn("shearX",               0,  LSI);
        shearY            = addColumn("shearY",               0,  LSI);
        horizontalSRID    = addColumn("horizontalSRID",           LSI);
        horizontalExtent  = addColumn("horizontalExtent",         LS ); // Will rely on trigger for insertion.
        verticalSRID      = addColumn("verticalSRID",      null, LFSI);
        verticalOrdinates = addColumn("verticalOrdinates", null, LFSI);
        if (database.isSpatialEnabled()) {
            horizontalExtent.setFunction("Box2D", LS);
        }
        final QueryType[] F = {FILTERED_LIST};
        byIdentifier        = addParameter(identifier, SELECT, EXISTS);
        byWidth             = addParameter(width,          F);
        byHeight            = addParameter(height,         F);
        byScaleX            = addParameter(scaleX,         F);
        byScaleY            = addParameter(scaleY,         F);
        byTranslateX        = addParameter(translateX,     F);
        byTranslateY        = addParameter(translateY,     F);
        byShearX            = addParameter(shearX,         F);
        byShearY            = addParameter(shearY,         F);
        byHorizontalSRID    = addParameter(horizontalSRID, F);
    }
}
