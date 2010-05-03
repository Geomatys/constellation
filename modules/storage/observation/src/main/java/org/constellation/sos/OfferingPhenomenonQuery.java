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

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

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
        super (database, "offering_phenomenons", "sos");
        //final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        
        idOffering          = addMandatoryColumn("id_offering", slie);
        phenomenon          = addOptionalColumn("phenomenon",  null, slie);
        compositePhenomenon = addOptionalColumn("composite_phenomenon",  null, slie);
        
        byOffering            = addParameter(idOffering, SELECT, LIST, EXISTS);
        byPhenomenon          = addParameter(phenomenon,  SELECT, EXISTS);
        byCompositePhenomenon = addParameter(compositePhenomenon,  SELECT, EXISTS);
    }
}
