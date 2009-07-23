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
package org.constellation.sos;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;
/**
 *
 * @author Guilhem Legal
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
        final QueryType[] sil  = {SELECT, INSERT, LIST};
        final QueryType[] siel = {SELECT,INSERT, EXISTS, LIST};
        id                   = addColumn("id",                     siel);
        name                 = addColumn("name",                   sil);
        description          = addColumn("description",            sil);
        srsName              = addColumn("srs_name",               sil);
        eventTimeBegin       = addColumn("event_time_begin",       sil);
        eventTimeEnd         = addColumn("event_time_end",         sil);
        boundedBy            = addColumn("bounded_by",             sil);
        resultModelNamespace = addColumn("result_model_namespace", sil);
        resultModelLocalPart = addColumn("result_model_localpart", sil);
        responseFormat       = addColumn("response_format",        sil);
        
        byId                 = addParameter(id, SELECT, EXISTS);
    }


}
