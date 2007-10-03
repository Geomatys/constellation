/*
 * Sicade - Systémes intégrés de connaissances pour l'aide à la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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


package net.sicade.observation;

import net.sicade.catalog.Column;
import net.sicade.catalog.Database;
import net.sicade.catalog.Parameter;
import net.sicade.catalog.Query;
import static net.sicade.catalog.QueryType.*;
import net.sicade.catalog.QueryType;

/**
 * The query to execute for a {@link SamplingPointTable}.
 *
 * @author Guilhem Legal
 */
public class SamplingPointQuery extends Query {
    
    
    /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, name, description, sampledFeature,pointIdentifier, srsName, srsDimension, positionValue;
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;   
    
   /**
    * Creates a new query for the specified database.
    *
    * @param database The database for which this query is created.
    */
    public SamplingPointQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        
        identifier             = addColumn   ("sampling_points", "id",                 usage);
        name                   = addColumn   ("sampling_points", "name",               usage);
        description            = addColumn   ("sampling_points", "description",        usage);
        sampledFeature         = addColumn   ("sampling_points", "sampled_feature",    usage);
        pointIdentifier        = addColumn   ("sampling_points", "point_id",           usage);
        srsName                = addColumn   ("sampling_points", "point_srsname",      usage);
        srsDimension           = addColumn   ("sampling_points", "point_srsdimension", usage);
        positionValue          = addColumn   ("sampling_points", "pos_value",          usage);
        
        byIdentifier  = addParameter(identifier, SELECT);
    }
    
}
