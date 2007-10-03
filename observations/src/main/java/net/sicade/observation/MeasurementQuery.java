
package net.sicade.observation;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;

/**
 * The query to execute for a {@link MeasurementTable}.
 *
 * @author Guilhem Legal
 */
public class MeasurementQuery extends Query{
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, featureOfInterest, featureOfInterestPoint, procedure, observedProperty, observedPropertyComposite,
            distribution, samplingTimeBegin, samplingTimeEnd, result, resultDefinition, description;
    // quality, , observationMetadata, procedureTime, procedureParameter,
 
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public MeasurementQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT};
        name                      = addColumn("measurements", "name",                       usage);
        description               = addColumn("measurements", "description",                usage);
        featureOfInterest         = addColumn("measurements", "feature_of_interest",        usage);
        featureOfInterestPoint    = addColumn("measurements", "feature_of_interest_point",  usage);
        procedure                 = addColumn("measurements", "procedure",                  usage);
        observedProperty          = addColumn("measurements", "observed_property",          usage);
        observedPropertyComposite = addColumn("measurements", "observed_property_composite",usage);
        distribution              = addColumn("measurements", "distribution",               usage);
        samplingTimeBegin         = addColumn("measurements", "sampling_time_begin",        usage);
        samplingTimeEnd           = addColumn("measurements", "sampling_time_end",          usage);
        result                    = addColumn("measurements", "result",                     usage);
        resultDefinition          = addColumn("measurements", "result_definition",          usage);
/*
        observationMetadata = addColumn("measurements", "observationMetadata", usage);
        quality             = addColumn("measurements", "quality",             usage);
        result              = addColumn("measurements", "result",              usage);
        procedureTime       = addColumn("measurements", "procedureTime",       usage);
        procedureParameter  = addColumn("measurements", "procedureParameter",  usage);*/
                
        
        byName = addParameter(name, SELECT);
    }
    
}
