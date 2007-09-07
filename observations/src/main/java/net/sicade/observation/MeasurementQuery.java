
package net.sicade.observation;

import net.sicade.catalog.Database;
import net.sicade.observation.sql.*;

/**
 * The query to execute for a {@link MeasurementTable}.
 *
 * @author Guilhem Legal
 */
public class MeasurementQuery extends ObservationQuery{
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public MeasurementQuery(final Database database) {
        super(database);
    }
    
}
