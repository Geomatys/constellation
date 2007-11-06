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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import org.geotools.resources.Utilities;

/**
 * Connexion vers la table des {@linkplain AnyScalarEntry dataRecord field}.
 * 
 * 
 * @author Guilhem Legal
 * @version $Id:
 */
public class AnyScalarTable extends SingletonTable<AnyScalarEntry>{
    
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
    protected AnyScalarEntry createEntry(final ResultSet results) throws SQLException {
        final AnyScalarQuery query = (AnyScalarQuery) super.query;
        return new AnyScalarEntry(
                results.getString(indexOf(query.idDataRecord )),
                results.getString(indexOf(query.name )),
                results.getString(indexOf(query.definition )),
                results.getString(indexOf(query.type )),
                results.getString(indexOf(query.uom )),
                results.getString(indexOf(query.value)));
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
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
    public synchronized String getIdentifier(final AnyScalarEntry field, String blockId) throws SQLException, CatalogException {
        final AnyScalarQuery query  = (AnyScalarQuery) super.query;
        String id;
        if (field.getName() != null) {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.byIdDataBlock), blockId);
            statement.setString(indexOf(query.idDataRecord),  field.getIdDataRecord());
            statement.setString(indexOf(query.name),          field.getName());
            ResultSet result = statement.executeQuery();
            if(result.next())
                return field.getName();
            else
                id = field.getName();
        } else {
            id = searchFreeIdentifier("field");
        }
        
        PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString(indexOf(query.idDataRecord), field.getIdDataRecord());
        statement.setString(indexOf(query.idDataBlock),  blockId);
        statement.setString(indexOf(query.name),         id);
        statement.setString(indexOf(query.definition),   field.getDefinition());
        statement.setString(indexOf(query.type),         field.getType());
        statement.setString(indexOf(query.uom),          field.getUom());
        statement.setString(indexOf(query.value),        (String)field.getValue());
        insertSingleton(statement);
     
        return id;
    }
}
