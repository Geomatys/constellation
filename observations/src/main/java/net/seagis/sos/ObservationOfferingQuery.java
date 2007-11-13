
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
        super (database);
        final QueryType[] SIL  = {SELECT, INSERT, LIST};
        final QueryType[] SIEL = {SELECT,INSERT, EXISTS, LIST};
        id                = addColumn("observation_offerings", "id",                   SIEL);
        name              = addColumn("observation_offerings", "name",                 SIL);
        srsName           = addColumn("observation_offerings", "srs_name",             SIL);
        description       = addColumn("observation_offerings", "description",          SIL);
        eventTimeBegin    = addColumn("observation_offerings", "event_time_begin",     SIL);
        eventTimeEnd      = addColumn("observation_offerings", "event_time_end",       SIL);
        boundedBy         = addColumn("observation_offerings", "bounded_by",           SIL);
        resultModel       = addColumn("observation_offerings", "result_model",         SIL);
        responseFormat    = addColumn("observation_offerings", "response_format",      SIL);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }


}
