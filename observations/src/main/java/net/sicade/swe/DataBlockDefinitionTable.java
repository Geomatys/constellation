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

package net.sicade.swe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;

/**
 * Connexion vers la table des {@linkplain DataBlockDefinition dataBlockDefintion}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockDefinitionTable extends SingletonTable<DataBlockDefinition>{
    
    /**
     * Connexion vers la table des {@linkplain TextBlock text block encoding}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected TextBlockTable textBlockEncodings;
    
    /**
     * Connexion vers la table des {@linkplain TextBlock text block encoding}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected SimpleDataRecordTable dataRecords;
    
    
    /**
     * Construit une table des data blocks.
     *
     * @param  database Connexion vers la base de données.
     */
    public DataBlockDefinitionTable(final Database database) {
          this(new DataBlockDefinitionQuery(database)); 
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private DataBlockDefinitionTable(final DataBlockDefinitionQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    protected DataBlockDefinition createEntry(final ResultSet results) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query = (DataBlockDefinitionQuery) super.query;
        String idDataBlock = results.getString(indexOf(query.id));
        
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
        }
        dataRecords.setIdDataBlock(idDataBlock);
        Collection<SimpleDataRecord> entries = dataRecords.getEntries();
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        TextBlock encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataBlockDefinitionEntry(idDataBlock, entries, encoding);
    }
    
}
