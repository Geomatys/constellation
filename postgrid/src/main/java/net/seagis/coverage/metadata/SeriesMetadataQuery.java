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
 * The query to execute for a {@link SeriesMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadaaQuery.java  $
 */
final class SeriesMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column id, seriesName, legendURI, pubDate, pocId, version, forecast,
        themekey1, themekey2, themekey3, themekey4, themekey5, themekey6, themekey7, themekey8;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SeriesMetadataQuery(final Database database) {
        super(database, "SeriesMetadata");
        final QueryType[] SL   = {SELECT, LIST};
        final QueryType[] SLEI = {SELECT, LIST, EXISTS, INSERT};
        id =               addColumn("id",        SLEI);
        seriesName =       addColumn("seriesName",       null,   SL  );
        legendURI =        addColumn("legendURI",        null,   SL  );
        pubDate =          addColumn("pubDate",          null,   SL  );
        pocId =            addColumn("pocId",            null,   SL  );
        version =          addColumn("version",          null,   SL  );
        forecast =         addColumn("forecast",         null,   SL  );
        themekey1 =        addColumn("themekey1",        null,   SL  );
        themekey2 =        addColumn("themekey2",        null,   SL  );
        themekey3 =        addColumn("themekey3",        null,   SL  );
        themekey4 =        addColumn("themekey4",        null,   SL  );
        themekey5 =        addColumn("themekey5",        null,   SL  );
        themekey6 =        addColumn("themekey6",        null,   SL  );
        themekey7 =        addColumn("themekey7",        null,   SL  );
        themekey8 =        addColumn("themekey8",        null,   SL  );
        byName    = addParameter(id, SELECT, EXISTS);
    }
}
