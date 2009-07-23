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
package org.constellation.sampling;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import static org.constellation.catalog.QueryType.*;
import org.constellation.catalog.QueryType;

/**
 * The query to execute for a {@link SamplingPointTable}.
 *
 * @author Guilhem Legal
 */
public class SamplingPointQuery extends Query {
    
    
    /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, name, description, sampledFeature,pointIdentifier, srsName, srsDimension, positionValueX, positionValueY;
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;  
    
   /**
    * Creates a new query for the specified database.
    *
    * @param database The database for which this query is created.
    */
    public SamplingPointQuery(final Database database) {
        super(database, "sampling_points");
        //final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        final QueryType[] sli   = {SELECT, LIST, INSERT};
        final QueryType[] slif  = {SELECT, LIST, INSERT, FILTERED_LIST};
        final QueryType[] slief = {SELECT, LIST, INSERT, EXISTS, FILTERED_LIST};
        
        identifier             = addColumn   ("id",                 slief);
        name                   = addColumn   ("name",               slif);
        description            = addColumn   ("description",        sli);
        sampledFeature         = addColumn   ("sampled_feature",    sli);
        pointIdentifier        = addColumn   ("point_id",           sli);
        srsName                = addColumn   ("point_srsname",      sli);
        srsDimension           = addColumn   ("point_srsdimension", sli);
        positionValueX         = addColumn   ("x_value",            sli);
        positionValueY         = addColumn   ("y_value",            sli);
        
        byIdentifier  = addParameter(identifier, SELECT, EXISTS);
        byName        = addParameter(name, FILTERED_LIST);
    }
    
}
