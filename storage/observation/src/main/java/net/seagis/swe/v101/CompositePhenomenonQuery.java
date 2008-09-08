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
package net.seagis.swe.v101;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import static net.seagis.catalog.QueryType.*;
import net.seagis.catalog.QueryType;


/**
 * The query to execute for a {@link CompositePhenomenonTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class CompositePhenomenonQuery extends Query{
    
    /** 
     * Column to appear after the {@code "SELECT"} clause.
     * we forget the attribute base for now 
     */
    protected final Column identifier, name, remarks, dimension;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public CompositePhenomenonQuery(final Database database) {
        super(database, "composite_phenomenons");
        final QueryType[] SIL = {SELECT, INSERT, LIST};
        final QueryType[] SIEL = {SELECT, INSERT, EXISTS, LIST};
        
        identifier = addColumn ("id",          SIEL);
        name       = addColumn ("name",        SIL);
        remarks    = addColumn ("description", SIL);
        dimension  = addColumn ("dimension",   SIL);
        
        byName     = addParameter(identifier, SELECT, EXISTS);
    }
    
}
