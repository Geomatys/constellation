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
package org.constellation.gml.v311;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import static org.geotoolkit.internal.sql.table.QueryType.*;
import org.geotoolkit.internal.sql.table.QueryType;

/**
 * The query to execute for a {@link LineStringTable}.
 *
 * @author Guilhem Legal
 */
public class LineStringQuery extends Query {
    
    
    /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, xValue, yValue, zValue;
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;
    
   /**
    * Creates a new query for the specified database.
    *
    * @param database The database for which this query is created.
    */
    public LineStringQuery(final Database database) {
        super(database, "linestring", "observation");
        //final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        final QueryType[] sli   = {SELECT, LIST, INSERT};
        //final QueryType[] slif  = {SELECT, LIST, INSERT, FILTERED_LIST};
        final QueryType[] slief = {SELECT, LIST, INSERT, EXISTS};
        
        identifier             = addMandatoryColumn("id",                 slief);
        xValue                 = addMandatoryColumn("x",                  sli);
        yValue                 = addMandatoryColumn("y",                  sli);
        zValue                 = addOptionalColumn ("z",            null, sli);

        byIdentifier  = addParameter(identifier, SELECT, EXISTS, LIST);
        
    }
    
}
