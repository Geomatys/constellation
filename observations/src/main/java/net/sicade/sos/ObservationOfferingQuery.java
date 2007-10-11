
package net.sicade.sos;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;
/**
 *
 * @author legal
 */
public class ObservationOfferingQuery extends Query {
    
      /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, name, srsName, description, eventTimeBegin, 
            eventTimeEnd, boundedBy, resultModel, 
            responseFormat, responseMode;
    
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
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        id                = addColumn("observation_offerings", "id",                  SLIE);
        name              = addColumn("observation_offerings", "name",                SLIE);
        srsName           = addColumn("observation_offerings", "srs_name",            SLI);
        description       = addColumn("observation_offerings", "description",         SLI);
        eventTimeBegin    = addColumn("observation_offerings", "event_time_begin",    SLI);
        eventTimeEnd      = addColumn("observation_offerings", "event_time_end",      SLI);
        boundedBy         = addColumn("observation_offerings", "bounded_by",          SLI);
        resultModel       = addColumn("observation_offerings", "resultModel",         SLI);
        responseFormat    = addColumn("observation_offerings", "responseFormat",      SLI);
        responseMode      = addColumn("observation_offerings", "responseMode",        SLI);
        
        
        byId         = addParameter(id, SELECT, EXISTS);
    }


}
