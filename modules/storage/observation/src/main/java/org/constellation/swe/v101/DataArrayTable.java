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
package org.constellation.swe.v101;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.AbstractEncodingType;
import org.geotoolkit.swe.xml.v101.DataArrayType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import org.geotoolkit.swe.xml.v101.TextBlockType;

/**
 * Connexion vers la table des {@linkplain DataArray dataArray}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataArrayTable extends SingletonTable<DataArrayType> implements Cloneable {
    
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
        super(query,query.byIdArray);
    }

     /**
     * Construit une nouvelle table non partagée
     */
    private DataArrayTable(final DataArrayTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected DataArrayTable clone() {
        return new DataArrayTable(this);
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected DataArrayType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException {
        final DataArrayQuery query = (DataArrayQuery) super.query;
        final String idArray = results.getString(indexOf(query.idArray));
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
        }
        dataRecords.setIdDataBlock(idArray);
        //for data array there is only one data record
        final Iterator<SimpleDataRecordType> it = dataRecords.getEntries().iterator();
        SimpleDataRecordType entry = null;
        if (it.hasNext()) {
            entry = it.next();
        }
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        final AbstractEncodingType encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataArrayType(idArray,
                                  results.getInt(indexOf(query.elementCount)),
                                  idArray,
                                  entry, 
                                  encoding,
                                  null);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du dataArray passée en parametre si non-null)
     * et enregistre le nouveau dataArray dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le dataArray a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final DataArrayType array) throws SQLException, CatalogException {
        final DataArrayQuery query  = (DataArrayQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            //first we get the identifier form sub object
            int count = 0;
            if (array.getElementCount() != null && array.getElementCount().getCount() != null) {
                count = array.getElementCount().getCount().getValue();
            }

            if (textBlockEncodings == null) {
                    textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
            }
            final String textBlockIdentifier = textBlockEncodings.getIdentifier((TextBlockType)array.getEncoding());

            try {
                if (array.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.idArray), array.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return array.getId();
                    } else {
                        id = array.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "dataArray");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.idArray), id);
                statement.statement.setInt(indexOf(query.elementCount), count);

                statement.statement.setString(indexOf(query.encoding), textBlockIdentifier);

                if (dataRecords == null) {
                    dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
                }
                dataRecords.setIdDataBlock(id);
                statement.statement.setString(indexOf(query.elementType), dataRecords.getIdentifier((SimpleDataRecordType)array.getElementType(), id));

                updateSingleton(statement.statement);
                release(lc, statement);

                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
    
    /**
     * We ovveride this method because it will probably have a very large number af dataArray
     * and the super method is bounded.
     * @param base the base for identifier (base
     * @return a new id for the dataArray
     
    @Override
    public String searchFreeIdentifier(String base) throws CatalogException, SQLException {
        final PreparedStatement stmt = this.getStatement("SELECT COUNT(\"id_array_definition\") FROM \"observation\".\"data_array_definition\"");
        final ResultSet result = stmt.executeQuery();
        result.next();
        final int nbLine = result.getInt(1);
        return base + '-' + nbLine;
    }*/
}
