/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.catalog;

import org.constellation.catalog.Database;
import org.constellation.catalog.Column;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


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
    final Column identifier, width, height, scaleX, scaleY, translateX, translateY,
            shearX, shearY, horizontalSRID, verticalSRID, verticalOrdinates;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byIdentifier, byWidth, byHeight, byScaleX, byScaleY,
            byTranslateX, byTranslateY, byShearX, byShearY, byHorizontalSRID;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public GridGeometryQuery(final Database database) {
        super(database, "GridGeometries");
        final QueryType[] lfsei = {LIST, FILTERED_LIST, SELECT, EXISTS, INSERT};
        final QueryType[] lfsi  = {LIST, FILTERED_LIST, SELECT,         INSERT};
        final QueryType[] lsi   = {LIST,                SELECT,         INSERT};
        //final QueryType[] ls    = {LIST,                SELECT                };
        final QueryType[] se    = {                     SELECT, EXISTS        };
        final QueryType[] f     = {      FILTERED_LIST                        };
        identifier        = addColumn("identifier",             lfsei);
        width             = addColumn("width",                    lsi);
        height            = addColumn("height",                   lsi);
        scaleX            = addColumn("scaleX",                   lsi);
        scaleY            = addColumn("scaleY",                   lsi);
        translateX        = addColumn("translateX",               lsi);
        translateY        = addColumn("translateY",               lsi);
        shearX            = addColumn("shearX",               0,  lsi);
        shearY            = addColumn("shearY",               0,  lsi);
        horizontalSRID    = addColumn("horizontalSRID",           lsi);
        verticalSRID      = addColumn("verticalSRID",      null, lfsi);
        verticalOrdinates = addColumn("verticalOrdinates", null, lfsi);
        byIdentifier      = addParameter(identifier,    se);
        byWidth           = addParameter(width,          f);
        byHeight          = addParameter(height,         f);
        byScaleX          = addParameter(scaleX,         f);
        byScaleY          = addParameter(scaleY,         f);
        byTranslateX      = addParameter(translateX,     f);
        byTranslateY      = addParameter(translateY,     f);
        byShearX          = addParameter(shearX,         f);
        byShearY          = addParameter(shearY,         f);
        byHorizontalSRID  = addParameter(horizontalSRID, f);
    }
}
