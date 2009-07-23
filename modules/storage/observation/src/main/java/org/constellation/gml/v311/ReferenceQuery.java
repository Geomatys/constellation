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
package org.constellation.gml.v311;

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
        final QueryType[] sli   = {SELECT, LIST, INSERT};
        final QueryType[] slief = {SELECT, INSERT, LIST, EXISTS, FILTERED_LIST};
        idReference = addColumn("id_reference", slief);
        actuate     = addColumn("actuate"     , sli);
        arcrole     = addColumn("arcrole"     , sli);
        href        = addColumn("href"        , sli);
        role        = addColumn("role"        , sli);
        show        = addColumn("show"        , sli);
        title       = addColumn("title"       , sli);
        type        = addColumn("type"        , sli);
        owns        = addColumn("owns"        , sli);
        
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
