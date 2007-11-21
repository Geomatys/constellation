
package net.seagis.observation;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;

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
        super(database, "measurements");
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIE = {SELECT, INSERT, EXISTS};
        
        name                      = addColumn("name",                        SIE);
        description               = addColumn("description",                 SI);
        featureOfInterest         = addColumn("feature_of_interest",         SI);
        featureOfInterestPoint    = addColumn("feature_of_interest_point",   SI);
        procedure                 = addColumn("procedure",                   SI);
        observedProperty          = addColumn("observed_property",           SI);
        observedPropertyComposite = addColumn("observed_property_composite", SI);
        distribution              = addColumn("distribution",                SI);
        samplingTimeBegin         = addColumn("sampling_time_begin",         SI);
        samplingTimeEnd           = addColumn("sampling_time_end",           SI);
        result                    = addColumn("result",                      SI);
        resultDefinition          = addColumn("result_definition",           SI);
/*
        observationMetadata       = addColumn("observationMetadata",         SI);
        quality                   = addColumn("quality",                     SI);
        result                    = addColumn("result",                      SI);
        procedureTime             = addColumn("procedureTime",               SI);
        procedureParameter        = addColumn("procedureParameter",          SI);*/
                
        
        byName = addParameter(name, SELECT, EXISTS);
    }
    
}
