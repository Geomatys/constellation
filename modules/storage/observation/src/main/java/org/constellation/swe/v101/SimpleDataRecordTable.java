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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.SingletonTable;
import org.constellation.catalog.QueryType;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.util.Utilities;

/**
 *  Connexion vers la table des {@linkplain SimpleDataRecord simpleDataRecord}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SimpleDataRecordTable extends SingletonTable<SimpleDataRecordEntry>{
    
    /**
     * identifiant secondaire de la table
     * (identifiant du DataBlock contenant le dataRecord qui possede ce champ).
     */
    private String idDataBlock;
    
    
    /**
     * Connexion vers la table des {@linkplain DataRecordField dataRecord field}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private AnyScalarTable fields;
    
    /**
     * Construit une table des data record.
     *
     * @param  database Connexion vers la base de données.
     */
    public SimpleDataRecordTable(final Database database) {
        this(new SimpleDataRecordQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private SimpleDataRecordTable(final SimpleDataRecordQuery query) {
        super(query);
        setIdentifierParameters(query.byIdDataRecord, null);
    }
    
    /**
     * Un constructeur qui prend en parametre un table partagée afin d'en creer
     * une qui ne l'ai pas.
     */
    public SimpleDataRecordTable(final SimpleDataRecordTable table) {
        super(table);
    }
    
    /**
     * retourne l'identifiant du DataBlock contenant le dataRecord qui possede ce champ.
     */
    public String getIdDataBlock() {
        return idDataBlock;
    }
    
    /**
     * Modifie l'identifiant du dataBlock si il est different de l'actuel.
     *
     * @param idDataBlock le nouvel identifiant du dataBlock.
     */
    public synchronized void setIdDataBlock(final String idDataBlock) {
        if (!Utilities.equals(this.idDataBlock, idDataBlock)) {
            this.idDataBlock = idDataBlock;
            fireStateChanged("idDataBlock");
        }
    }
    
    /**
     * Construit un data record pour l'enregistrement courant.
     */
    protected SimpleDataRecordEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        final String idDataBlock = results.getString(indexOf(query.idBlock));
        final String idDataRecord = results.getString(indexOf(query.idDataRecord));
        
        if (fields == null) {
            fields = getDatabase().getTable(AnyScalarTable.class);
            fields = new AnyScalarTable(fields);
        }
       
        fields.setIdDataBlock(idDataBlock);
        fields.setIdDataRecord(idDataRecord);
        final Collection<AnyScalarPropertyType> scalars = fields.getEntries();
        
        return new SimpleDataRecordEntry(idDataBlock, idDataRecord,
                results.getString(indexOf(query.definition)),
                results.getBoolean(indexOf(query.fixed)), 
                scalars);
        
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        if (!type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byIdBlock), idDataBlock);
        
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du data record passée en parametre si non-null)
     * et enregistre le nouveau data record dans la base de donnée si il n'y est pas deja.
     *
     * @param datarecord le data record a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final SimpleDataRecordEntry datarecord, String dataBlockId) throws SQLException, CatalogException {
        final SimpleDataRecordQuery query  = (SimpleDataRecordQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (datarecord.getId() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.idBlock),      dataBlockId);
                statement.setString(indexOf(query.idDataRecord), datarecord.getId());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return datarecord.getId();
                } else {
                    id = datarecord.getId();
                }
            } else {
                id = searchFreeIdentifier("datarecord");
            }
        
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.idDataRecord), id);
            statement.setString(indexOf(query.idBlock),      dataBlockId);
            statement.setString(indexOf(query.definition),   datarecord.getDefinition());
            statement.setBoolean(indexOf(query.fixed),       datarecord.isFixed());
            updateSingleton(statement);
         
            if (fields == null) {
                fields = getDatabase().getTable(AnyScalarTable.class);
                fields = new AnyScalarTable(fields);
                fields.setIdDataBlock(dataBlockId);
                fields.setIdDataRecord(id);
            } else {
                fields.setIdDataBlock(dataBlockId);
                fields.setIdDataRecord(id);
            }
            final Iterator<AnyScalarPropertyType> i = datarecord.getField().iterator();
        
            while (i.hasNext()) {
               fields.getIdentifier(i.next(), dataBlockId, id);
            }
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
