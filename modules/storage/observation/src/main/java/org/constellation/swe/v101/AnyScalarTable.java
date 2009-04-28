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
import java.util.logging.Logger;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotoolkit.swe.xml.v101.AbstractDataComponentEntry;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.BooleanType;
import org.geotoolkit.swe.xml.v101.QuantityType;
import org.geotoolkit.swe.xml.v101.TimeType;
import org.geotoolkit.util.Utilities;

/**
 * Connexion vers la table des {@linkplain AnyScalarEntry dataRecord field}.
 * 
 * 
 * @author Guilhem Legal
 * @version $Id:
 */
public class AnyScalarTable extends SingletonTable<AnyScalarPropertyType>{
    
    /**
     * A logger (debugging purpose)
     */
    private Logger logger = Logger.getLogger("anyScalarTable");
    
    /**
     * identifiant secondaire de la table
     * (identifiant du DataBlock contenant le dataRecord qui possede ce champ).
     */
    private String idDataBlock;
    
    /**
     * identifiant secondaire de la table.
     * (identifiant du dataRecord qui possede ce champ).
     */
    private String idDataRecord;
    
    /**
     * Construit une table des dataRecord field.
     *
     * @param  database Connexion vers la base de données.
     */
    public AnyScalarTable(final Database database) {
        this(new AnyScalarQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private AnyScalarTable(final AnyScalarQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
     /**
     * Un constructeur qui prend en parametre un table partagée afin d'en creer
     * une qui ne l'ai pas.
     */
    public AnyScalarTable(final AnyScalarTable table) {
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
    public void setIdDataBlock(final String idDataBlock) {
        if (!Utilities.equals(this.idDataBlock, idDataBlock)) {
            this.idDataBlock = idDataBlock;
            fireStateChanged("idDataBlock");
        }
    }
    
    /**
     * Retourne identifiant du dataRecord qui possede ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }
    
    /**
     * Modifie l'identifiant du dataRecord si il est different de l'actuel.
     *
     * @param idDataRecoed le nouvel identifiant du dataRecord.
     */
    public void setIdDataRecord(final String idDataRecord) {
        if (!Utilities.equals(this.idDataRecord, idDataRecord)) {
            this.idDataRecord = idDataRecord;
            fireStateChanged("idDataRecord");
        }
    }
    
    /**
     * Construit un data block pour l'enregistrement courant.
     */
    @Override
    protected AnyScalarPropertyType createEntry(final ResultSet results) throws SQLException {
        final AnyScalarQuery query = (AnyScalarQuery) super.query;
        AbstractDataComponentEntry component = null;
        if (results.getString(indexOf(query.type)).equals("Quantity")) {
            component = new QuantityType(results.getString(indexOf(query.definition )), 
                                              results.getString(indexOf(query.uomCode)),
                                              results.getString(indexOf(query.uomHref))); 
        } else if (results.getString(indexOf(query.type)).equals("Time")) {
            component = new TimeType(results.getString(indexOf(query.definition )), 
                                     results.getString(indexOf(query.uomCode)),
                                     results.getString(indexOf(query.uomHref))); 
        } else if (results.getString(indexOf(query.type)).equals("Boolean")) {
            component = new BooleanType(results.getString(indexOf(query.definition )), 
                                        results.getBoolean(indexOf(query.value))); 
        } 
        return new AnyScalarPropertyType(
                results.getString(indexOf(query.idDataRecord )),
                results.getString(indexOf(query.name )),
                component);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final AnyScalarQuery query = (AnyScalarQuery) super.query;
        if(!type.equals(QueryType.INSERT)){
            statement.setString(indexOf(query.byIdDataRecord), idDataRecord);
            statement.setString(indexOf(query.byIdDataBlock), idDataBlock);
        }
        
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du champ passée en parametre si non-null)
     * et enregistre le nouveau champ dans la base de donnée si il n'y est pas deja.
     *
     * @param datarecord le data record a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final AnyScalarPropertyType field, String blockId, String dataRecordId) throws SQLException, CatalogException {
        final AnyScalarQuery query  = (AnyScalarQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (field.getName() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.byIdDataBlock), blockId);
                statement.setString(indexOf(query.idDataRecord),  dataRecordId);
                statement.setString(indexOf(query.name),          field.getName());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return field.getName();
                } else {
                    id = field.getName();
                }
            } else {
                id = searchFreeIdentifier("field");
            }
        
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.idDataRecord), dataRecordId);
            statement.setString(indexOf(query.idDataBlock),  blockId);
            statement.setString(indexOf(query.name),         id);
            if (field.getComponent() != null)
                statement.setString(indexOf(query.definition),   field.getComponent().getDefinition());
            else
                statement.setNull(indexOf(query.definition), java.sql.Types.VARCHAR);   
            
            if (field.getComponent() instanceof QuantityType) {
                QuantityType q = (QuantityType) field.getComponent();
            
                statement.setString(indexOf(query.type), "Quantity");
                if ( q.getUom().getCode() != null)
                    statement.setString(indexOf(query.uomCode), q.getUom().getCode());
                else
                    statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);
            
                if ( q.getUom().getHref() != null)
                    statement.setString(indexOf(query.uomHref), q.getUom().getHref());
                else
                    statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);
            
                statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);
            
            } else if (field.getComponent() instanceof TimeType) {
                TimeType t = (TimeType) field.getComponent();
            
                statement.setString(indexOf(query.type), "Time");
                if ( t.getUom() != null && t.getUom().getCode() != null)
                    statement.setString(indexOf(query.uomCode), t.getUom().getCode());
                else
                    statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);
            
                if ( t.getUom() != null && t.getUom().getHref() != null)
                    statement.setString(indexOf(query.uomHref), t.getUom().getHref());
                else
                    statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);
            
                statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);
            
            } else if (field.getComponent() instanceof BooleanType) {
                BooleanType b = (BooleanType) field.getComponent();
            
                statement.setString(indexOf(query.type), "Boolean");
                if (b.isValue() != null)
                    statement.setBoolean(indexOf(query.value), b.isValue());
                else
                    statement.setNull(indexOf(query.value), java.sql.Types.BOOLEAN);
            
                statement.setNull(indexOf(query.uomHref), java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.uomCode), java.sql.Types.VARCHAR);
            
            } else {
                throw new CatalogException("Unexpected scalar Type:" + field.getComponent());            
            }
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
