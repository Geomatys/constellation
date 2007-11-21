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
public class ReferenceQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idReference, actuate, arcrole, href, role, show, title, type, owns;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdReference;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ReferenceQuery(final Database database) {
        super(database, "references");
        final QueryType[] SI  = {SELECT, INSERT};
        final QueryType[] SIE = {SELECT, INSERT, EXISTS};
        idReference = addColumn("id_reference", SIE);
        actuate     = addColumn("actuate"     , SI);
        arcrole     = addColumn("arcrole"     , SI);
        href        = addColumn("href"        , SI);
        role        = addColumn("role"        , SI);
        show        = addColumn("show"        , SI);
        title       = addColumn("title"       , SI);
        type        = addColumn("type"        , SI);
        owns        = addColumn("owns"        , SI);
        
        byIdReference          = addParameter(idReference, SELECT, EXISTS);
    }
    
}
