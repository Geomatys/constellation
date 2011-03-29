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
import java.util.Collection;
import java.util.Iterator;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.DataBlockDefinitionType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import org.geotoolkit.swe.xml.v101.TextBlockType;
import org.geotoolkit.swe.xml.v101.AbstractEncodingPropertyType;

/**
 * Connexion vers la table des {@linkplain DataBlockDefinition dataBlockDefintion}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockDefinitionTable extends SingletonTable<DataBlockDefinitionType> implements Cloneable {
    
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
        super(query, query.byId);
    }

     /**
     * Construit une nouvelle table non partagée
     */
    private DataBlockDefinitionTable(final DataBlockDefinitionTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected DataBlockDefinitionTable clone() {
        return new DataBlockDefinitionTable(this);
    }

    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected DataBlockDefinitionType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query = (DataBlockDefinitionQuery) super.query;
        final String idDataBlock = results.getString(indexOf(query.id));
        
        if (dataRecords == null) {
            dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
        }
        dataRecords.setIdDataBlock(idDataBlock);
        final Collection<SimpleDataRecordType> entries = dataRecords.getEntries();
        
        if (textBlockEncodings == null) {
            textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
        }
        
        final TextBlockType encoding = textBlockEncodings.getEntry(results.getString(indexOf(query.encoding)));
        
        return new DataBlockDefinitionType(idDataBlock, entries, encoding);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du datablockDefinition passée en parametre si non-null)
     * et enregistre le nouveau datablockDefinition dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le datablockDefinition a inserer dans la base de donnée.
     */
    public String getIdentifier(final DataBlockDefinitionType databloc) throws SQLException, CatalogException {
        final DataBlockDefinitionQuery query  = (DataBlockDefinitionQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (databloc.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.id), databloc.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return databloc.getId();
                    } else {
                        id = databloc.getId();
                    }
                    result.close();
                } else {
                    id = searchFreeIdentifier(lc, "datablockDef");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.id), id);

                if (textBlockEncodings == null) {
                    textBlockEncodings = getDatabase().getTable(TextBlockTable.class);
                }
                final AbstractEncodingPropertyType encProp = databloc.getEncoding();
                statement.statement.setString(indexOf(query.encoding), textBlockEncodings.getIdentifier((TextBlockType) encProp.getEncoding()));
                updateSingleton(statement.statement);
                release(lc, statement);
                
                if (dataRecords == null) {
                    dataRecords = getDatabase().getTable(SimpleDataRecordTable.class);
                    dataRecords.setIdDataBlock(id);
                } else {
                    dataRecords.setIdDataBlock(id);
                }
                final Iterator i = databloc.getComponents().iterator();
                while (i.hasNext()) {
                    dataRecords.getIdentifier((SimpleDataRecordType) i.next(), id);
                }
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
