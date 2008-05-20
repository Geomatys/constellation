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
 * The query to execute for a {@link LayerMetadataTable}.
 * 
 * This implementation is specific to FGDC metadata standards.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadaaQuery.java  $
 */
final class LayerMetadataQuery extends Query {
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column layerMetaName, layerName, abbrTitle, shortTitle, 
            longTitle, parameterName, parameterType, description, longDescription,
            dataSource, purpose, supplementalInfo, updateFrequency, useConstraint;

    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public LayerMetadataQuery(final Database database) {
        super(database, "LayerMetadata");
        final QueryType[] SL   = {SELECT, LIST};
        final QueryType[] SLEI = {SELECT, LIST, EXISTS, INSERT};
        layerMetaName =   addColumn("id",        SLEI);
        layerName =        addColumn("layerName",       null,   SL  );
        abbrTitle =        addColumn("abbrTitle",       null,   SL  );
        shortTitle =       addColumn("shortTitle",      null,   SL  );
        longTitle =        addColumn("longTitle",       null,   SL  );
        parameterName =    addColumn("parameterName",   null,   SL  );
        parameterType =    addColumn("parameterType",   null,   SL  );
        description =       addColumn("description",      null,   SL  );
        longDescription =  addColumn("longDescription", null,   SL  );
        dataSource =       addColumn("dataSource",      null,   SL  );
        purpose =           addColumn("purpose",          null,   SL  );
        supplementalInfo = addColumn("supplementalInfo",null,   SL  );
        updateFrequency =  addColumn("updateFrequency", null,   SL  );
        useConstraint =    addColumn("useConstraint",   null,   SL  );
        byName    = addParameter(layerMetaName, SELECT, EXISTS);
    }
}
