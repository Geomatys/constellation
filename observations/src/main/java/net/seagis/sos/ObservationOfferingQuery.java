
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
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIE = {SELECT,INSERT, EXISTS};
        id                = addColumn("observation_offerings", "id",                   SIE);
        name              = addColumn("observation_offerings", "name",                 SI);
        srsName           = addColumn("observation_offerings", "srs_name",             SI);
        description       = addColumn("observation_offerings", "description",          SI);
        eventTimeBegin    = addColumn("observation_offerings", "event_time_begin",     SI);
        eventTimeEnd      = addColumn("observation_offerings", "event_time_end",       SI);
        boundedBy         = addColumn("observation_offerings", "bounded_by",           SI);
        resultModel       = addColumn("observation_offerings", "result_model",         SI);
        responseFormat    = addColumn("observation_offerings", "response_format",      SI);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }


}
