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

import org.constellation.coverage.catalog.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.constellation.catalog.Database;
import org.constellation.catalog.BoundedSingletonTable;
import org.constellation.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain LayerMetadata layer-level metadata}.
 * 
 * Metadata specific to the {@linkplain Layer layer}.
 *
 * @author Sam Hiatt
 * @version $Id$
 */
public class LayerMetadataTable extends BoundedSingletonTable<LayerMetadata> {
    
    /**
     * Creates a layer metadata table.
     *
     * @param database Connection to the database.
     */
    public LayerMetadataTable(final Database database) {
        this(new LayerMetadataQuery(database));
    }

    /**
     * Constructs a new {@code LayerTable} from the specified query.
     */
    private LayerMetadataTable(final LayerMetadataQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a layer metadata table using the same initial configuration than the specified table.
     */
    public LayerMetadataTable(final LayerMetadataTable table) {
        super(table);
    }

    /**
     * Creates a layer metadata from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected LayerMetadata createEntry(final ResultSet results) throws CatalogException, SQLException {
        final LayerMetadataQuery query = (LayerMetadataQuery) super.query;
        final String layerMetaName =   results.getString(indexOf(query.layerMetaName  ));
        final String layerName =        results.getString(indexOf(query.layerName  ));
        final String abbrTitle =        results.getString(indexOf(query.abbrTitle  ));
        final String shortTitle =       results.getString(indexOf(query.shortTitle  ));
        final String longTitle =        results.getString(indexOf(query.longTitle  ));
        final String parameterName =    results.getString(indexOf(query.parameterName  ));
        final String parameterType =    results.getString(indexOf(query.parameterType  ));
        final String description =       results.getString(indexOf(query.description ));
        final String longDescription =  results.getString(indexOf(query.longDescription  ));
        final String dataSource =       results.getString(indexOf(query.dataSource  ));
        final String purpose =           results.getString(indexOf(query.purpose ));
        final String supplementalInfo = results.getString(indexOf(query.supplementalInfo  ));
        final String updateFrequency =  results.getString(indexOf(query.updateFrequency  ));
        final String useConstraint =    results.getString(indexOf(query.useConstraint  ));
        
        final LayerMetadataEntry entry = new LayerMetadataEntry(
                layerMetaName, layerName, abbrTitle, shortTitle, longTitle, 
                parameterName, parameterType, description, longDescription, dataSource, 
                purpose, supplementalInfo, updateFrequency, useConstraint);
        return entry;
    }

    /**
     * Clears this table cache.
     */
    @Override
    public synchronized void flush() {
        super.flush();
    }
}
