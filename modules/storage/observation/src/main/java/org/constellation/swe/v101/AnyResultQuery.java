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
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;

/**
 * The query to execute for a {@link AnyResultTable}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyResultQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column idResult, reference, values, definition;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdResult, byValues, byRef, byDefinition;
    
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public AnyResultQuery(final Database database) {
        super(database, "any_results");
        final QueryType[] SSI  = {SELECT, SELECT_BY_NUMBER, INSERT, FILTERED_LIST};
        final QueryType[] SSIE = {SELECT, SELECT_BY_NUMBER, INSERT, EXISTS};
        final QueryType[] SSE   = {SELECT,SELECT_BY_NUMBER,  EXISTS};

        idResult     = addColumn("id_result",  SSE);
        reference    = addColumn("reference",  SSI);
        values       = addColumn("values",     SSI);
        definition   = addColumn("definition", SSI);

        byIdResult   = addParameter(idResult,  SELECT, EXISTS, SELECT_BY_NUMBER);
        byValues     = addParameter(values, FILTERED_LIST);
        byRef        = addParameter(reference, FILTERED_LIST);
        byDefinition = addParameter(definition, FILTERED_LIST);
    }
    
}
