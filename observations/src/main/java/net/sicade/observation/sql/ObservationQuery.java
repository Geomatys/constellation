package net.sicade.observation.sql;

// Sicade dependencies
import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;

/**
 * The query to execute for a {@link ObservationTable}.
 *
 * @author Guilhem Legal
 */
public class ObservationQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, featureOfInterest, observedProperty, procedure, distribution,
            quality, result, samplingTime, observationMetadata, resultDefinition, procedureTime, 
            procedureParameter, remarks;

 
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ObservationQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name                = addColumn("observations", "name",                usage);
        featureOfInterest   = addColumn("observations", "featureOfInterest",   usage);
        observedProperty    = addColumn("observations", "observedProperty",    usage);
        procedure           = addColumn("observations", "procedure",           usage);
        distribution        = addColumn("observations", "distribution",        usage);
        quality             = addColumn("observations", "quality",             usage);
        result              = addColumn("observations", "result",              usage);
        samplingTime        = addColumn("observations", "samplingTime",        usage);
        observationMetadata = addColumn("observations", "observationMetadata", usage);
        resultDefinition    = addColumn("observations", "resultDefinition",    usage);
        procedureTime       = addColumn("observations", "procedureTime",       usage);
        procedureParameter  = addColumn("observations", "procedureParameter",  usage);
        remarks             = addColumn("observations", "description",         usage);
        
    }
    
}
