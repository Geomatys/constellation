/*
 * Ecocast - NASA Ames Research Center
 * (C) 2008, Ecocast
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
package org.constellation.coverage.metadata;

import org.constellation.catalog.Database;
import org.constellation.catalog.Column;
import org.constellation.catalog.Parameter;
import org.constellation.catalog.Query;
import org.constellation.catalog.QueryType;
import static org.constellation.catalog.QueryType.*;


/**
 * The query to execute for a {@link CoverageMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class CoverageMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column id, coverageId, uri, creationDate, seriesName;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public CoverageMetadataQuery(final Database database) {
        super(database, "CoverageMetadata");
        final QueryType[] sl   = {SELECT, LIST};
        final QueryType[] slei = {SELECT, LIST, EXISTS, INSERT};
        
        coverageId =       addColumn("coverageId",               slei);
        id =               addColumn("id",               null,   sl  );
        uri =              addColumn("uri",              null,   sl  );
        creationDate =     addColumn("creationDate",     null,   sl  );
        seriesName =       addColumn("seriesName",       null,   sl  );
        byName    = addParameter(coverageId, SELECT, EXISTS);
    }
}
