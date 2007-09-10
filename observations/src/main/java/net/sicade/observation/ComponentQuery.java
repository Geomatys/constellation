/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.observation;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import static net.sicade.catalog.QueryType.*;
import net.sicade.catalog.QueryType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ComponentQuery extends Query{
    
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
        super (database);
        final QueryType[] usage = {SELECT, LIST};
        idCompositePhenomenon  = addColumn("components", "composite_phenomenon", usage);
        idComponent            = addColumn("components", "component",  usage);
        
        byComposite = addParameter(idCompositePhenomenon, SELECT, LIST);
        byComponent = addParameter(idComponent,  SELECT);
    }
    
}
