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
public class OfferingPhenomenonQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, phenomenon;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, byPhenomenon;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingPhenomenonQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering   = addColumn("offering_phenomenons", "id_offering", SLIE);
        phenomenon   = addColumn("offering_phenomenons", "phenomenon",  SLIE);
        
        byOffering   = addParameter(idOffering, SELECT, LIST, EXISTS);
        byPhenomenon = addParameter(phenomenon,  SELECT, EXISTS);
    }
}
