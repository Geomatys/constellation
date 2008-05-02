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
    protected final Parameter byIdReference, byActuate, byArcrole, byHref, byRole, byShow, byTitle, byType, byOwns;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ReferenceQuery(final Database database) {
        super(database, "references");
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        final QueryType[] SLIEF = {SELECT, INSERT, LIST, EXISTS, FILTERED_LIST};
        idReference = addColumn("id_reference", SLIEF);
        actuate     = addColumn("actuate"     , SLI);
        arcrole     = addColumn("arcrole"     , SLI);
        href        = addColumn("href"        , SLI);
        role        = addColumn("role"        , SLI);
        show        = addColumn("show"        , SLI);
        title       = addColumn("title"       , SLI);
        type        = addColumn("type"        , SLI);
        owns        = addColumn("owns"        , SLI);
        
        byIdReference = addParameter(idReference, SELECT, EXISTS);
        byActuate     = addParameter(actuate,     FILTERED_LIST);
        byArcrole     = addParameter(arcrole,     FILTERED_LIST);
        byHref        = addParameter(href,        FILTERED_LIST);
        byRole        = addParameter(role,        FILTERED_LIST);
        byShow        = addParameter(show,        FILTERED_LIST);
        byTitle       = addParameter(title,       FILTERED_LIST);
        byType        = addParameter(type,        FILTERED_LIST);
        byOwns        = addParameter(owns,        FILTERED_LIST);
        
    }
    
}
