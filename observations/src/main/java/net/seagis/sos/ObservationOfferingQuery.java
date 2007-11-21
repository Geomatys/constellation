
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
    protected final Column id, name, srsName, description, eventTimeBegin, 
            eventTimeEnd, boundedBy, resultModel, responseFormat;
    
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
        id                = addColumn("id",                   SIEL);
        name              = addColumn("name",                 SIL);
        srsName           = addColumn("srs_name",             SIL);
        description       = addColumn("description",          SIL);
        eventTimeBegin    = addColumn("event_time_begin",     SIL);
        eventTimeEnd      = addColumn("event_time_end",       SIL);
        boundedBy         = addColumn("bounded_by",           SIL);
        resultModel       = addColumn("result_model",         SIL);
        responseFormat    = addColumn("response_format",      SIL);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }


}
