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
package net.seagis.coverage.metadata;

import net.seagis.catalog.Database;
import net.seagis.catalog.Column;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import net.seagis.catalog.QueryType;
import static net.seagis.catalog.QueryType.*;


/**
 * The query to execute for a {@link CoverageMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadaaQuery.java  $
 */
final class CoverageMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, coverageId, uri, creationDate, seriesName;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public CoverageMetadataQuery(final Database database) {
        super(database, "CoverageMetadata");
        final QueryType[] SL   = {SELECT, LIST};
        final QueryType[] SLEI = {SELECT, LIST, EXISTS, INSERT};
        id =               addColumn("id",        SLEI);
        coverageId =       addColumn("coverageId",       null,   SL  );
        uri =              addColumn("uri",              null,   SL  );
        creationDate =     addColumn("creationDate",     null,   SL  );
        seriesName =       addColumn("seriesName",       null,   SL  );
        byName    = addParameter(id, SELECT, EXISTS);
    }
}
