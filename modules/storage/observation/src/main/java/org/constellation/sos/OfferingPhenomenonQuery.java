/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.sos;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;

/**
 *
 * @author Guilhem Legal
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
        super (database, "offering_phenomenons");
        //final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering          = addColumn("id_offering", slie);
        phenomenon          = addColumn("phenomenon",  slie);
        compositePhenomenon = addColumn("composite_phenomenon",  slie);
        
        byOffering            = addParameter(idOffering, SELECT, LIST, EXISTS);
        byPhenomenon          = addParameter(phenomenon,  SELECT, EXISTS);
        byCompositePhenomenon = addParameter(compositePhenomenon,  SELECT, EXISTS);
    }
}
