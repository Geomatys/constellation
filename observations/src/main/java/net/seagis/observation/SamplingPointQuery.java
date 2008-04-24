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


package net.seagis.observation;

import net.seagis.catalog.Column;
import net.seagis.catalog.Database;
import net.seagis.catalog.Parameter;
import net.seagis.catalog.Query;
import static net.seagis.catalog.QueryType.*;
import net.seagis.catalog.QueryType;

/**
 * The query to execute for a {@link SamplingPointTable}.
 *
 * @author Guilhem Legal
 */
public class SamplingPointQuery extends Query {
    
    
    /** 
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column identifier, name, description, sampledFeature,pointIdentifier, srsName, srsDimension, positionValueX, positionValueY;
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byIdentifier;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;  
    
   /**
    * Creates a new query for the specified database.
    *
    * @param database The database for which this query is created.
    */
    public SamplingPointQuery(final Database database) {
        super(database, "sampling_points");
        final QueryType[] SLIE = {SELECT, LIST, INSERT, EXISTS};
        final QueryType[] SLI  = {SELECT, LIST, INSERT};
        
        identifier             = addColumn   ("id",                 SLIE);
        name                   = addColumn   ("name",               SLIE);
        description            = addColumn   ("description",        SLI);
        sampledFeature         = addColumn   ("sampled_feature",    SLI);
        pointIdentifier        = addColumn   ("point_id",           SLI);
        srsName                = addColumn   ("point_srsname",      SLI);
        srsDimension           = addColumn   ("point_srsdimension", SLI);
        positionValueX         = addColumn   ("x_value",            SLI);
        positionValueY         = addColumn   ("y_value",            SLI);
        
        byIdentifier  = addParameter(identifier, SELECT);
        byName        = addParameter(name, EXISTS);
    }
    
}
