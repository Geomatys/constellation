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

import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.Column;
import static org.geotoolkit.internal.sql.table.QueryType.*;
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
        super (database, "envelopes","sos");
        final QueryType[] sli  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        id           = addMandatoryColumn("id",             slie);
        srsName      = addMandatoryColumn("srs_name",       sli);
        lowerCornerX = addMandatoryColumn("lower_corner_x", sli);
        lowerCornerY = addMandatoryColumn("lower_corner_y", sli);
        upperCornerX = addMandatoryColumn("upper_corner_x", sli);
        upperCornerY = addMandatoryColumn("upper_corner_y", sli);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }

}
