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
package org.constellation.observation;

import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;

/**
 * The query to execute for a {@link ProcessTable}.
 *
 * @author Guilhem Legal
 */
public class ProcessQuery extends Query{
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, remarks;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public ProcessQuery(final Database database) {
        super(database, "process");
        final QueryType[] SLI  = {SELECT, INSERT, LIST};
        final QueryType[] SLIE = {SELECT, EXISTS, INSERT, LIST};
        name    = addColumn   ("name",        SLIE);
        remarks = addColumn   ("description", SLI);

        byName  = addParameter(name, SELECT, EXISTS);
    }
    
}
