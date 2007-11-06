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
public class OfferingPhenomenonQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, phenomenon, compositePhenomenon;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, byPhenomenon, byCompositePhenomenon;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingPhenomenonQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering          = addColumn("offering_phenomenons", "id_offering", SLIE);
        phenomenon          = addColumn("offering_phenomenons", "phenomenon",  SLIE);
        compositePhenomenon = addColumn("offering_phenomenons", "composite_phenomenon",  SLIE);
        
        byOffering            = addParameter(idOffering, SELECT, LIST, EXISTS);
        byPhenomenon          = addParameter(phenomenon,  SELECT, EXISTS);
        byCompositePhenomenon = addParameter(compositePhenomenon,  SELECT, EXISTS);
    }
}
