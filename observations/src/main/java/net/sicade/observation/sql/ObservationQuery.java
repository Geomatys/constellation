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
    protected final Column name, featureOfInterest,  procedure, observedProperty, distribution, 
             samplingTime,resultDefinition,  description;
 // quality, result, observationMetadata, procedureTime, procedureParameter,
 
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ObservationQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name                = addColumn("observations", "name",                usage);
        description         = addColumn("observations", "description",         usage);
        featureOfInterest   = addColumn("observations", "feature_of_interest",   usage);
        procedure           = addColumn("observations", "procedure",           usage);
        observedProperty    = addColumn("observations", "observed_property",   usage);
        distribution        = addColumn("observations", "distribution",        usage);
        samplingTime        = addColumn("observations", "sampling_time",        usage);
        resultDefinition    = addColumn("observations", "result_definition",    usage);
/*
        observationMetadata = addColumn("observations", "observationMetadata", usage);
        quality             = addColumn("observations", "quality",             usage);
        result              = addColumn("observations", "result",              usage);
        procedureTime       = addColumn("observations", "procedureTime",       usage);
        procedureParameter  = addColumn("observations", "procedureParameter",  usage);*/
        
        
    }
    
}
