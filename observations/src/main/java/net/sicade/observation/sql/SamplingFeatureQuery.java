/* Created on 3 septembre 2007, 12:32 */

package net.sicade.observation.sql;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;

/**
 * The query to execute for a {@link SamplingFeatureTable}.
 *
 * @author Guilhem Legal
 */
public class SamplingFeatureQuery extends Query {
    
     /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, name, remarks, relatedSamplingFeature, 
            relatedObservation, sampledFeature, surveyDetail;
     /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SamplingFeatureQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name                    = addColumn   ("samplingFeatures", "name",        usage);
        remarks                 = addColumn   ("samplingFeatures", "description", usage);
        identifier              = addColumn   ("samplingFeatures", "identifier", usage);
        relatedSamplingFeature  = addColumn   ("samplingFeatures", "relatedSamplingFeature", usage);
        relatedObservation      = addColumn   ("samplingFeatures", "relatedObservation", usage);
        sampledFeature          = addColumn   ("samplingFeatures", "sampledFeature", usage);
        surveyDetail            = addColumn   ("samplingFeatures", "surveyDetail", usage);
        
        
        byIdentifier  = addParameter(identifier, SELECT);
        name.setOrdering("ASC");
    }
    
}
