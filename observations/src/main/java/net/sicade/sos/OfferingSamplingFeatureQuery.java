

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
public class OfferingSamplingFeatureQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, samplingFeature;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, bySamplingFeature;
    
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
        
        byOffering        = addParameter(idOffering, SELECT, LIST, EXISTS);
        bySamplingFeature = addParameter(samplingFeature,  SELECT, EXISTS);
    }

}
