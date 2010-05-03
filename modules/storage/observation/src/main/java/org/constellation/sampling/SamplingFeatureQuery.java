/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.sampling;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

/**
 * The query to execute for a {@link SamplingFeatureTable}.
 *
 * @author Guilhem Legal
 */
public class SamplingFeatureQuery extends Query {
    
     /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, name, description, sampledFeature;
     /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;

    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SamplingFeatureQuery(final Database database) {
        super(database, "sampling_features", "observation");
        final QueryType[] sli = {SELECT, LIST, INSERT};
        final QueryType[] slie = {SELECT, LIST, INSERT, EXISTS, LIST_ID};
        
        identifier              = addMandatoryColumn ("id",                    slie);
        name                    = addMandatoryColumn ("name",                  sli);
        description             = addOptionalColumn  ("description",     null, sli);
        sampledFeature          = addOptionalColumn  ("sampled_feature", null, sli);
        
        byIdentifier  = addParameter(identifier, SELECT, EXISTS);
    }
    
}
