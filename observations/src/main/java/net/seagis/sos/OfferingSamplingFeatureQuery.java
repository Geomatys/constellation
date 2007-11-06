

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
public class OfferingSamplingFeatureQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, samplingFeature, samplingPoint;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, bySamplingFeature, bySamplingPoint;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingSamplingFeatureQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering      = addColumn("offering_sampling_features", "id_offering", SLIE);
        samplingFeature = addColumn("offering_sampling_features", "sampling_feature",  SLIE);
        samplingPoint   = addColumn("offering_sampling_features", "sampling_point",  SLIE);
        
        byOffering        = addParameter(idOffering, SELECT, LIST, EXISTS);
        bySamplingFeature = addParameter(samplingFeature,  SELECT, EXISTS);
        bySamplingPoint   = addParameter(samplingPoint,  SELECT, EXISTS);
    }

}
