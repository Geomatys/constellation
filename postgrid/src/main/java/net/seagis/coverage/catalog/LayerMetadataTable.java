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
package net.seagis.coverage.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.seagis.catalog.Database;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.CatalogException;


/**
 * Connection to a table of {@linkplain LayerMetadata layer-level metadata}.
 * 
 * Metadata specific to the {@linkplain Layer layer}.
 *
 * @author Sam Hiatt
 * @version $Id: LayerMetadaaTable.java  $
 */
public class LayerMetadataTable extends BoundedSingletonTable<LayerMetadata> {
    /**
     * Connection to the table of layers. Will be created when first needed.
     */
    private Layer layer;
    
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
        final String layer_meta_name =   results.getString(indexOf(query.layer_meta_name ));
        final String layer_name =        results.getString(indexOf(query.layer_name ));
        final String abbr_title =        results.getString(indexOf(query.abbr_title ));
        final String short_title =       results.getString(indexOf(query.short_title ));
        final String long_title =        results.getString(indexOf(query.long_title ));
        final String parameter_name =    results.getString(indexOf(query.parameter_name ));
        final String parameter_type =    results.getString(indexOf(query.parameter_type ));
        final String description =       results.getString(indexOf(query.description ));
        final String long_description =  results.getString(indexOf(query.long_description ));
        final String data_source =       results.getString(indexOf(query.data_source ));
        final String purpose =           results.getString(indexOf(query.purpose ));
        final String supplemental_info = results.getString(indexOf(query.supplemental_info ));
        final String update_frequency =  results.getString(indexOf(query.update_frequency ));
        final String use_constraint =    results.getString(indexOf(query.use_constraint ));
        
        final LayerMetadataEntry entry = new LayerMetadataEntry(
                layer_meta_name, layer_name, abbr_title, short_title, long_title, 
                parameter_name, parameter_type, description, long_description, data_source, 
                purpose, supplemental_info, update_frequency, use_constraint);
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
