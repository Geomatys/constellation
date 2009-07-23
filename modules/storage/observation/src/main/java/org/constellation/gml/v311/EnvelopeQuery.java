/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;
/**
 * Represent a rectangle in the space. 
 * 
 * @author legal
 */
public class EnvelopeQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, srsName, lowerCornerX, lowerCornerY, upperCornerX, upperCornerY;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public EnvelopeQuery(final Database database) {
        super (database, "envelopes");
        final QueryType[] sli  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        id           = addColumn("id",             slie);
        srsName      = addColumn("srs_name",       sli);
        lowerCornerX = addColumn("corner_x", sli);
        lowerCornerY = addColumn("lower_corner_y", sli);
        upperCornerX = addColumn("upper_corner_x", sli);
        upperCornerY = addColumn("upper_corner_y", sli);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }

}
