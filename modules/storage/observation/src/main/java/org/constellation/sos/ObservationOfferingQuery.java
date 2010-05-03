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

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;
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
        super (database, "observation_offerings", "sos");
        final QueryType[] sil  = {SELECT, INSERT, LIST};
        final QueryType[] siel = {SELECT,INSERT, EXISTS, LIST, LIST_ID};
        id                   = addMandatoryColumn("id",                         siel);
        name                 = addMandatoryColumn("name",                        sil);
        description          = addOptionalColumn("description",            null, sil);
        srsName              = addOptionalColumn("srs_name",               null, sil);
        eventTimeBegin       = addOptionalColumn("event_time_begin",       null, sil);
        eventTimeEnd         = addOptionalColumn("event_time_end",         null, sil);
        boundedBy            = addOptionalColumn("bounded_by",             null, sil);
        resultModelNamespace = addOptionalColumn("result_model_namespace", null, sil);
        resultModelLocalPart = addOptionalColumn("result_model_localpart", null, sil);
        responseFormat       = addOptionalColumn("response_format",        null, sil);
        
        byId                 = addParameter(id, SELECT, EXISTS);
    }


}
