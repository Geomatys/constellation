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

package net.seagis.swe.v101;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;

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
    @Override
    protected DataBlockDefinition createEntry(final ResultSet results) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query = (DataBlockDefinitionQuery) super.query;
        String idDataBlock = results.getString(indexOf(query.id));
        
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
            dataRecords = new SimpleDataRecordTable(dataRecords);
        }
        dataRecords.setIdDataBlock(idDataBlock);
        Collection<SimpleDataRecordEntry> entries = dataRecords.getEntries();
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        TextBlockEntry encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataBlockDefinitionEntry(idDataBlock, entries, encoding);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du datablockDefinition passée en parametre si non-null)
     * et enregistre le nouveau datablockDefinition dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le datablockDefinition a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final DataBlockDefinitionEntry databloc) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query  = (DataBlockDefinitionQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (databloc.getId() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.id), databloc.getId());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return databloc.getId();
                } else {
                    id = databloc.getId();
                }
            } else {
                id = searchFreeIdentifier("datablockDef");
            }
        
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.id), id);

            if (textBlockEncodings == null) {
                textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
            }
            statement.setString(indexOf(query.encoding), textBlockEncodings.getIdentifier((TextBlockEntry)databloc.getEncoding()));
            updateSingleton(statement);
        
            if (dataRecords == null) {
                dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
                dataRecords = new SimpleDataRecordTable(dataRecords);
                dataRecords.setIdDataBlock(id);
            } else {
                dataRecords.setIdDataBlock(id);
            }
            Iterator i = databloc.getComponents().iterator();
            while (i.hasNext()) {
                dataRecords.getIdentifier((SimpleDataRecordEntry) i.next(), id);
            }
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
