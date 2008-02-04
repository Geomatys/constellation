

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
        super (database, "offering_sampling_features");
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering      = addColumn("id_offering", SLIE);
        samplingFeature = addColumn("sampling_feature",  SLIE);
        
        byOffering        = addParameter(idOffering, SELECT, LIST, EXISTS);
        bySamplingFeature = addParameter(samplingFeature,  SELECT, EXISTS);
    }

}
