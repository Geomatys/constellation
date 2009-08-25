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
package org.constellation.swe.v101;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;

/**
 * The query to execute for a {@link DataArrayTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataArrayQuery extends Query{
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idArray, encoding, elementCount, elementType;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdArray, byElementCount, byEncoding, byElementType;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public DataArrayQuery(final Database database) {
        super(database, "data_array_definition", "observation");
        final QueryType[] si   = {SELECT, INSERT};
        final QueryType[] sief = {SELECT, INSERT, EXISTS, FILTERED_LIST};
        idArray        = addColumn ("id_array_definition", sief);
        encoding       = addColumn ("encoding"           , si);
        elementCount   = addColumn ("element_count"      , si);
        elementType    = addColumn ("elementType"        , si);

        byIdArray      = addParameter(idArray,      SELECT, EXISTS);
        byElementCount = addParameter(elementCount, FILTERED_LIST);
        byElementType  = addParameter(elementType,  FILTERED_LIST);
        byEncoding     = addParameter(encoding,     FILTERED_LIST);
    }
    
}
