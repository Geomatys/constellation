/*
 * OfferingProcedureQuery.java
 * 
 * Created on 10 oct. 2007, 17:03:19
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class OfferingResponseModeQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, mode;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, byMode;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingResponseModeQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering  = addColumn("offering_response_modes", "id_offering", SLIE);
        mode        = addColumn("offering_response_modes", "mode",  SLIE);
        
        byOffering  = addParameter(idOffering, SELECT, LIST, EXISTS);
        byMode      = addParameter(mode,  SELECT, EXISTS);
    }

}
