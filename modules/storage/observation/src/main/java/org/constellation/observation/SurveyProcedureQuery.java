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
package org.constellation.observation;

import org.geotoolkit.internal.sql.table.Column;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.Parameter;
import org.geotoolkit.internal.sql.table.Query;
import org.geotoolkit.internal.sql.table.QueryType;
import static org.geotoolkit.internal.sql.table.QueryType.*;

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
        super(database, "SurveyProcedure", "observation");
        final QueryType[] usage = {SELECT, LIST};
        name              = addMandatoryColumn   ("name",              usage);
        operator          = addOptionalColumn   ("operator",          null ,usage);
        elevationDatum    = addOptionalColumn   ("elevationDatum",    null ,usage);
        elevationMethod   = addOptionalColumn   ("elevationMethod",   null ,usage);
        elevationAccuracy = addOptionalColumn   ("elevationAccuracy", null ,usage);
        geodeticDatum     = addOptionalColumn   ("geodeticDatum",     null ,usage);
        positionMethod    = addOptionalColumn   ("positionMethod",    null ,usage);
        positionAccuracy  = addOptionalColumn   ("positionAccuracy",  null ,usage);
        projection        = addOptionalColumn   ("projection",        null ,usage);
        surveyTime        = addOptionalColumn   ("surveyTime",        null ,usage);
        
        byName  = addParameter(name, SELECT);
    }
    
}
