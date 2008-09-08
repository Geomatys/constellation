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
package net.seagis.swe.v101;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;

/**
 * Connexion vers la table des {@linkplain DataArray dataArray}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataArrayTable extends SingletonTable<DataArrayEntry>{
    
    /**
     * Connexion vers la table des {@linkplain TextBlock text block encoding}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected TextBlockTable textBlockEncodings;
    
    /**
     * Connexion vers la table des {@linkplain DataRecord data record}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected SimpleDataRecordTable dataRecords;
    
    
    /**
     * Construit une table des data blocks.
     *
     * @param  database Connexion vers la base de données.
     */
    public DataArrayTable(final Database database) {
          this(new DataArrayQuery(database)); 
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private DataArrayTable(final DataArrayQuery query) {
        super(query);
        setIdentifierParameters(query.byIdArray, null);
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected DataArrayEntry createEntry(final ResultSet results) throws SQLException, CatalogException {
        final DataArrayQuery query = (DataArrayQuery) super.query;
        String idArray = results.getString(indexOf(query.idArray));
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
            dataRecords = new SimpleDataRecordTable(dataRecords);
        }
        dataRecords.setIdDataBlock(idArray);
        //for data array there is only one data record
        Iterator<SimpleDataRecordEntry> it = dataRecords.getEntries().iterator();
        SimpleDataRecordEntry entry = null;
        if (it.hasNext()) {
            entry = it.next();
        }
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        AbstractEncodingEntry encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataArrayEntry(idArray,
                                  results.getInt(indexOf(query.elementCount)),
                                  entry, encoding, null);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du dataArray passée en parametre si non-null)
     * et enregistre le nouveau dataArray dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le dataArray a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final DataArrayEntry array) throws SQLException, CatalogException {
        final DataArrayQuery query  = (DataArrayQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        //first we get the identifier form sub object
        int count = 0;
        if (array.getElementCount() != null && array.getElementCount().getCount() != null) {
            count = array.getElementCount().getCount().getValue();
        }
        
        if (textBlockEncodings == null) {
                textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        String textBlockIdentifier = textBlockEncodings.getIdentifier((TextBlockEntry)array.getEncoding());
            
        try {
            if (array.getId() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.idArray), array.getId());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return array.getId();
                } else {
                    id = array.getId();
                }
            } else {
                id = searchFreeIdentifier("dataArray");
            }
        
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.idArray), id);
            statement.setInt(indexOf(query.elementCount), count);

           statement.setString(indexOf(query.encoding), textBlockIdentifier);
            
            if (dataRecords == null) {
                dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
                dataRecords = new SimpleDataRecordTable(dataRecords);
            }
            dataRecords.setIdDataBlock(id);
            statement.setString(indexOf(query.elementType), dataRecords.getIdentifier((SimpleDataRecordEntry)array.getElementType(), id));
            
            updateSingleton(statement);
        
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
    /**
     * We ovveride this method because it will probably have a very large number af dataArray
     * and the super method is bounded.
     * @param base the base for identifier (base
     * @return a new id for the dataArray
     */
    @Override
    public String searchFreeIdentifier(String base) throws CatalogException, SQLException {
        PreparedStatement stmt = this.getStatement("SELECT COUNT(id_array_definition) FROM data_array_definition");
        ResultSet result = stmt.executeQuery();
        result.next();
        int nbLine = result.getInt(1);
        return base + '-' + nbLine;
    }
}
