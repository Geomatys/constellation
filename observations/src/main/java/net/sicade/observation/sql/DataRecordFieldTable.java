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
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.DataRecordFieldEntry;

/**
 * Connexion vers la table des {@linkplain DataRecordFieldEntry dataRecord field}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataRecordFieldTable extends SingletonTable<DataRecordFieldEntry>{
    
    /**
     * Construit une table des dataRecord field.
     *
     * @param  database Connexion vers la base de données.
     */
    public DataRecordFieldTable(final Database database) {
        super(new DataRecordFieldQuery(database));
    }
    
    /**
     * Construit un data block pour l'enregistrement courant.
     */
    protected DataRecordFieldEntry createEntry(final ResultSet results) throws SQLException {
        final DataRecordFieldQuery query = (DataRecordFieldQuery) super.query;
        return new DataRecordFieldEntry(results.getString(indexOf(query.idDataRecord )),
                                        results.getInt(indexOf(query.idField )),
                                        results.getString(indexOf(query.value)));
    }
    
}
