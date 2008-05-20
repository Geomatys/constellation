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

import net.seagis.coverage.catalog.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.seagis.catalog.Database;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain CoverageMetadata layer-level metadata}.
 * 
 * Metadata specific to the {@linkplain Layer layer}.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadaaTable.java  $
 */
public class CoverageMetadataTable extends BoundedSingletonTable<CoverageMetadata> {
    
    /**
     * Creates a layer metadata table.
     *
     * @param database Connection to the database.
     */
    public CoverageMetadataTable(final Database database) {
        this(new CoverageMetadataQuery(database));
    }

    /**
     * Constructs a new {@code LayerTable} from the specified query.
     */
    private CoverageMetadataTable(final CoverageMetadataQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a layer metadata table using the same initial configuration than the specified table.
     */
    public CoverageMetadataTable(final CoverageMetadataTable table) {
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
    protected CoverageMetadata createEntry(final ResultSet results) throws CatalogException, SQLException {
        final CoverageMetadataQuery query = (CoverageMetadataQuery) super.query;
        final String id =               results.getString(indexOf(query.id));
        final String coverageId =       results.getString(indexOf(query.coverageId));
        final String uri =              results.getString(indexOf(query.uri));
        final String creationDate =     results.getString(indexOf(query.creationDate));
        final String seriesName =       results.getString(indexOf(query.seriesName));
        final CoverageMetadataEntry entry = new CoverageMetadataEntry(id, coverageId, 
                uri, creationDate, seriesName);
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
