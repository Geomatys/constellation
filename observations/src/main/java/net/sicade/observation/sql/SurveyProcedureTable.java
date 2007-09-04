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

package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.SurveyProcedureEntry;
import org.opengis.observation.sampling.SurveyProcedure;

/**
 * 
* Connexion vers la table des {@linkplain SurveyProcedure surveyProcedures}.
 *
 * @version $Id:
 *
 * @author Guilhem Legal
 */
public class SurveyProcedureTable extends SingletonTable<SurveyProcedure>{
    
   /**
    * Construit une table des survey procedures.
    * 
    * @param  database Connexion vers la base de données.
    */
    public SurveyProcedureTable(final Database database) {
         super(new SurveyProcedureQuery(database)); 
    }

    /**
     * Construit une survey procedure pour l'enregistrement courant.
     */
    protected SurveyProcedure createEntry(final ResultSet results) throws CatalogException, SQLException {
         final SurveyProcedureQuery query = (SurveyProcedureQuery) super.query;
         return new SurveyProcedureEntry(results.getString(indexOf(query.name   )),
                                         results.getString(indexOf(query.operator)),
                                         results.getString(indexOf(query.elevationAccuracy)),
                                         results.getString(indexOf(query.elevationDatum)),
                                         results.getString(indexOf(query.elevationMethod)),
                                         results.getString(indexOf(query.geodeticDatum)),
                                         results.getString(indexOf(query.positionMethod)),
                                         results.getString(indexOf(query.positionAccuracy)),
                                         results.getString(indexOf(query.projection)),
                                         results.getString(indexOf(query.surveyTime)));
    }
    
}
