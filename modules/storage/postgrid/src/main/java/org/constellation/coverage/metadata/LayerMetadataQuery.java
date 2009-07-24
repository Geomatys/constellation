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
 * The query to execute for a {@link LayerMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
final class LayerMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    final Column layerMetaName, layerName, abbrTitle, shortTitle, 
            longTitle, parameterName, parameterType, description, longDescription,
            dataSource, purpose, supplementalInfo, updateFrequency, useConstraint;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public LayerMetadataQuery(final Database database) {
        super(database, "LayerMetadata");
        final QueryType[] sl   = {SELECT, LIST};
        final QueryType[] slei = {SELECT, LIST, EXISTS, INSERT};

        layerName =        addColumn("layerName",               slei);
        layerMetaName =    addColumn("id",              null,   sl  );
        abbrTitle =        addColumn("abbrTitle",       null,   sl  );
        shortTitle =       addColumn("shortTitle",      null,   sl  );
        longTitle =        addColumn("longTitle",       null,   sl  );
        parameterName =    addColumn("parameterName",   null,   sl  );
        parameterType =    addColumn("parameterType",   null,   sl  );
        description =       addColumn("description",      null,   sl  );
        longDescription =  addColumn("longDescription", null,   sl  );
        dataSource =       addColumn("dataSource",      null,   sl  );
        purpose =           addColumn("purpose",          null,   sl  );
        supplementalInfo = addColumn("supplementalInfo",null,   sl  );
        updateFrequency =  addColumn("updateFrequency", null,   sl  );
        useConstraint =    addColumn("useConstraint",   null,   sl  );
        byName    = addParameter(layerName, SELECT, EXISTS);
    }
}
