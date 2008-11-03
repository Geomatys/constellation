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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.SingletonTable;
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
    * @param  database Connexion vers la base de donn�es.
    */
    public SurveyProcedureTable(final Database database) {
         super(new SurveyProcedureQuery(database)); 
    }

    /**
     * Construit une survey procedure pour l'enregistrement courant.
     */
    protected SurveyProcedure createEntry(final ResultSet results) throws CatalogException, SQLException {
         final SurveyProcedureQuery query = (SurveyProcedureQuery) super.query;
         return null;/* new SurveyProcedureEntry(results.getString(indexOf(query.name   )),
                                         results.getString(indexOf(query.operator)),
                                         results.getString(indexOf(query.elevationAccuracy)),
                                         results.getString(indexOf(query.elevationDatum)),
                                         results.getString(indexOf(query.elevationMethod)),
                                         results.getString(indexOf(query.geodeticDatum)),
                                         results.getString(indexOf(query.positionMethod)),
                                         results.getString(indexOf(query.positionAccuracy)),
                                         results.getString(indexOf(query.projection)),
                                         results.getString(indexOf(query.surveyTime)));*/
    }
    
}
