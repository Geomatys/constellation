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
    protected final Column layer_meta_name, layer_name, abbr_title, short_title, 
            long_title, parameter_name, parameter_type, description, long_description,
            data_source, purpose, supplemental_info, update_frequency, use_constraint;

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
        super(database, "layermetadata");
        final QueryType[] SL   = {SELECT, LIST};
        final QueryType[] SLEI = {SELECT, LIST, EXISTS, INSERT};
        layer_meta_name =   addColumn("layer_meta_name",        SLEI);
        layer_name =        addColumn("layer_name",       null,   SL  );
        abbr_title =        addColumn("abbr_title",       null,   SL  );
        short_title =       addColumn("short_title",      null,   SL  );
        long_title =        addColumn("long_title",       null,   SL  );
        parameter_name =    addColumn("parameter_name",   null,   SL  );
        parameter_type =    addColumn("parameter_type",   null,   SL  );
        description =       addColumn("description",      null,   SL  );
        long_description =  addColumn("long_description", null,   SL  );
        data_source =       addColumn("data_source",      null,   SL  );
        purpose =           addColumn("purpose",          null,   SL  );
        supplemental_info = addColumn("supplemental_info",null,   SL  );
        update_frequency =  addColumn("update_frequency", null,   SL  );
        use_constraint =    addColumn("use_constraint",   null,   SL  );
        byName    = addParameter(layer_meta_name, SELECT, EXISTS);
    }
}
