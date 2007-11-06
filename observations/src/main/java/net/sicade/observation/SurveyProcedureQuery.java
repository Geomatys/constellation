/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
import net.sicade.catalog.QueryType;
import static net.sicade.catalog.QueryType.*;

/**
 * The query to execute for a {@link SurveyProcedureTable}
 *
 * @author Guilhem Legal
 */
public class SurveyProcedureQuery extends Query {
    
    /**
     * Column to appear after the {@code "SELECT"} clause.
     */
    protected final Column name, operator, elevationDatum, elevationMethod, elevationAccuracy,
            geodeticDatum, positionMethod, positionAccuracy, projection, surveyTime;
    
    /**
     * Parameter to appear after the {@code "FROM"} clause.
     */
    protected final Parameter byName;
      
    /**
     * Creates a new query for the specified database.
     *
     * @param database The database for which this query is created.
     */
    public SurveyProcedureQuery(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name              = addColumn   ("SurveyProcedure", "name",              usage);
        operator          = addColumn   ("SurveyProcedure", "operator",          usage);
        elevationDatum    = addColumn   ("SurveyProcedure", "elevationDatum",    usage);
        elevationMethod   = addColumn   ("SurveyProcedure", "elevationMethod",   usage);
        elevationAccuracy = addColumn   ("SurveyProcedure", "elevationAccuracy", usage);
        geodeticDatum     = addColumn   ("SurveyProcedure", "geodeticDatum",     usage);
        positionMethod    = addColumn   ("SurveyProcedure", "positionMethod",    usage);
        positionAccuracy  = addColumn   ("SurveyProcedure", "positionAccuracy",  usage);
        projection        = addColumn   ("SurveyProcedure", "projection",        usage);
        surveyTime        = addColumn   ("SurveyProcedure", "surveyTime",        usage);
        
        byName  = addParameter(name, SELECT);
    }
    
}
