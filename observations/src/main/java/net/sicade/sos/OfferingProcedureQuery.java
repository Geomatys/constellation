/*
 * OfferingProcedureQuery.java
 * 
 * Created on 10 oct. 2007, 17:03:19
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class OfferingProcedureQuery extends Query {
    
     /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idOffering, procedure;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byOffering, byProcedure;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public OfferingProcedureQuery(final Database database) {
        super (database);
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering  = addColumn("offering_procedures", "id_offering", SLIE);
        procedure   = addColumn("offering_procedures", "procedure",  SLIE);
        
        byOffering  = addParameter(idOffering, SELECT, LIST, EXISTS);
        byProcedure = addParameter(procedure,  SELECT, EXISTS);
    }

}
