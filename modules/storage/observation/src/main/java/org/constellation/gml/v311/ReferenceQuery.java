/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import static org.geotoolkit.internal.sql.table.QueryType.*;
import org.geotoolkit.internal.sql.table.QueryType;

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
        super(database, "references", "observation");
        final QueryType[] sli   = {SELECT, LIST, INSERT};
        final QueryType[] slief = {SELECT, INSERT, LIST, EXISTS, LIST_ID};
        idReference = addMandatoryColumn("id_reference", slief);
        actuate     = addOptionalColumn("actuate", null, sli);
        arcrole     = addOptionalColumn("arcrole", null, sli);
        href        = addOptionalColumn("href",    null, sli);
        role        = addOptionalColumn("role",    null, sli);
        show        = addOptionalColumn("show",    null, sli);
        title       = addOptionalColumn("title",   null, sli);
        type        = addOptionalColumn("type",    null, sli);
        owns        = addOptionalColumn("owns",    null, sli);
        
        byIdReference = addParameter(idReference, SELECT, EXISTS);
        byActuate     = addParameter(actuate,     LIST_ID);
        byArcrole     = addParameter(arcrole,     LIST_ID);
        byHref        = addParameter(href,        LIST_ID);
        byRole        = addParameter(role,        LIST_ID);
        byShow        = addParameter(show,        LIST_ID);
        byTitle       = addParameter(title,       LIST_ID);
        byType        = addParameter(type,        LIST_ID);
        byOwns        = addParameter(owns,        LIST_ID);
        
    }
    
}
