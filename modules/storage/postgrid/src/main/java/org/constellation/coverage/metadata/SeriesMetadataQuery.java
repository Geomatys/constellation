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
 * The query to execute for a {@link SeriesMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class SeriesMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column id, seriesName, legendURI, pubDate, pocId, version, forecast,
        themekey1, themekey2, themekey3, themekey4, themekey5, themekey6, themekey7, themekey8;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SeriesMetadataQuery(final Database database) {
        super(database, "SeriesMetadata");
        final QueryType[] sl   = {SELECT, LIST};
        final QueryType[] slei = {SELECT, LIST, EXISTS, INSERT};

        seriesName =       addColumn("seriesName",               slei);
        id =               addColumn("id",               null,   sl  );
        legendURI =        addColumn("legendURI",        null,   sl  );
        pubDate =          addColumn("pubDate",          null,   sl  );
        pocId =            addColumn("pocId",            null,   sl  );
        version =          addColumn("version",          null,   sl  );
        forecast =         addColumn("forecast",         null,   sl  );
        themekey1 =        addColumn("themekey1",        null,   sl  );
        themekey2 =        addColumn("themekey2",        null,   sl  );
        themekey3 =        addColumn("themekey3",        null,   sl  );
        themekey4 =        addColumn("themekey4",        null,   sl  );
        themekey5 =        addColumn("themekey5",        null,   sl  );
        themekey6 =        addColumn("themekey6",        null,   sl  );
        themekey7 =        addColumn("themekey7",        null,   sl  );
        themekey8 =        addColumn("themekey8",        null,   sl  );
        byName    =        addParameter(seriesName, SELECT);
    }
}
