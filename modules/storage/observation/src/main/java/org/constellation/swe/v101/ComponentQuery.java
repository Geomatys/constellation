/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.swe.v101;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import static org.constellation.catalog.QueryType.*;
import org.constellation.catalog.QueryType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ComponentQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idCompositePhenomenon, idComponent;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byComposite, byComponent;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ComponentQuery(final Database database) {
        super (database, "components");
        //final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS};
        
        idCompositePhenomenon  = addColumn("composite_phenomenon", slie);
        idComponent            = addColumn("component",  slie);
        
        byComposite = addParameter(idCompositePhenomenon, SELECT, LIST, EXISTS);
        byComponent = addParameter(idComponent,  SELECT, EXISTS);
    }
    
}
