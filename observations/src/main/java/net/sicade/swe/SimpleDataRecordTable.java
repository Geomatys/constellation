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
import java.util.Collection;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import static net.sicade.catalog.QueryType.*;
import net.sicade.catalog.QueryType;
import org.geotools.resources.Utilities;

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
    public void setIdDataBlock(final String idDataBlock) {
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
        String idDataBlock = results.getString(indexOf(query.idBlock));
        String idDataRecord = results.getString(indexOf(query.idDataRecord));
        
        if (fields == null) {
            fields = getDatabase().getTable(AnyScalarTable.class);
            fields = new AnyScalarTable(fields);
        }
       
        fields.setIdDataBlock(idDataBlock);
        fields.setIdDataRecord(idDataRecord);
        Collection<AnyScalarEntry> entries = fields.getEntries();
        
        return new SimpleDataRecordEntry(idDataBlock, idDataRecord,
                results.getString(indexOf(query.definition)),
                results.getBoolean(indexOf(query.fixed)),entries);
        
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        final SimpleDataRecordQuery query = (SimpleDataRecordQuery) super.query;
        statement.setString(indexOf(query.byIdBlock), idDataBlock);
        
    }
    
}
