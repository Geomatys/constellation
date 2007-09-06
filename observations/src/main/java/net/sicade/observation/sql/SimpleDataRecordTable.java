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
import net.sicade.observation.SimpleDataRecord;
import net.sicade.observation.SimpleDataRecordEntry;
import static net.sicade.catalog.QueryType.*;

/**
 *  Connexion vers la table des {@linkplain SimpleDataRecord simpleDataRecord}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SimpleDataRecordTable extends SingletonTable<SimpleDataRecord>{
    
    /**
     * Connexion vers la table des {@linkplain DataRecordField dataRecord field}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected DataRecordFieldTable fields;
    
     /**
     * Construit une table des data record.
     *
     * @param  database Connexion vers la base de données.
     */
    public SimpleDataRecordTable(final Database database) {
         super(new SimpleDataRecordQuery(database)); 
    }

    /**
     * Construit un data record pour l'enregistrement courant.
     */
    protected SimpleDataRecord createEntry(final ResultSet results) throws CatalogException, SQLException {
          final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        return new SimpleDataRecordEntry(results.getString(indexOf(query.idBlock )),
                                         results.getString(indexOf(query.idDataRecord )),
                                         results.getString(indexOf(query.definition )),
                                         results.getBoolean(indexOf(query.fixed )),
                                         fields.getEntries(LIST));
                                         
    }
    
}
