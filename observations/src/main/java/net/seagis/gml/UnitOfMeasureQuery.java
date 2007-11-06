/*
 * Sicade - Systémes intégrés de connaissances pour l'aide é la décision en environnement
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
package net.seagis.gml;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import static net.seagis.catalog.QueryType.*;
import net.seagis.catalog.QueryType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class UnitOfMeasureQuery extends Query{
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, name, quantityType, unitSystem;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byId;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public UnitOfMeasureQuery(final Database database) {
        super(database);
        final QueryType[] SLI = {SELECT, LIST, INSERT};
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        
        id           = addColumn("unit_of_measures", "id",            SLIE);
        name         = addColumn("unit_of_measures", "name",          SLI);
        quantityType = addColumn("unit_of_measures", "quantity_type", SLI);
        unitSystem   = addColumn("unit_of_measures", "unit_system",   SLI);
        
        byId         = addParameter(id, SELECT, EXISTS);
    }
    
}
