
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
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIE = {SELECT, INSERT, EXISTS};
        
        name                      = addColumn("measurements", "name",                        SIE);
        description               = addColumn("measurements", "description",                 SI);
        featureOfInterest         = addColumn("measurements", "feature_of_interest",         SI);
        featureOfInterestPoint    = addColumn("measurements", "feature_of_interest_point",   SI);
        procedure                 = addColumn("measurements", "procedure",                   SI);
        observedProperty          = addColumn("measurements", "observed_property",           SI);
        observedPropertyComposite = addColumn("measurements", "observed_property_composite", SI);
        distribution              = addColumn("measurements", "distribution",                SI);
        samplingTimeBegin         = addColumn("measurements", "sampling_time_begin",         SI);
        samplingTimeEnd           = addColumn("measurements", "sampling_time_end",           SI);
        result                    = addColumn("measurements", "result",                      SI);
        resultDefinition          = addColumn("measurements", "result_definition",           SI);
/*
        observationMetadata       = addColumn("measurements", "observationMetadata",         SI);
        quality                   = addColumn("measurements", "quality",                     SI);
        result                    = addColumn("measurements", "result",                      SI);
        procedureTime             = addColumn("measurements", "procedureTime",               SI);
        procedureParameter        = addColumn("measurements", "procedureParameter",          SI);*/
                
        
        byName = addParameter(name, SELECT, EXISTS);
    }
    
}
