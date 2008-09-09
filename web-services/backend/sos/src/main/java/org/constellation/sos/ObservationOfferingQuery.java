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
package net.seagis.sos;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;
/**
 *
 * @author legal
 */
public class ObservationOfferingQuery extends Query {
    
      /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, name, description, eventTimeBegin, 
            eventTimeEnd, boundedBy, resultModelNamespace, resultModelLocalPart, 
            responseFormat, srsName;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ObservationOfferingQuery(final Database database) {
        super (database, "observation_offerings");
        final QueryType[] SIL  = {SELECT, INSERT, LIST};
        final QueryType[] SIEL = {SELECT,INSERT, EXISTS, LIST};
        id                   = addColumn("id",                     SIEL);
        name                 = addColumn("name",                   SIL);
        description          = addColumn("description",            SIL);
        srsName              = addColumn("srs_name",               SIL);
        eventTimeBegin       = addColumn("event_time_begin",       SIL);
        eventTimeEnd         = addColumn("event_time_end",         SIL);
        boundedBy            = addColumn("bounded_by",             SIL);
        resultModelNamespace = addColumn("result_model_namespace", SIL);
        resultModelLocalPart = addColumn("result_model_localpart", SIL);
        responseFormat       = addColumn("response_format",        SIL);
        
        byId                 = addParameter(id, SELECT, EXISTS);
    }


}
