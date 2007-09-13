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
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;

/**
 * Connexion vers la table des {@linkplain AnyResultEntry AnyResult}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyResultTable extends SingletonTable<AnyResultEntry>{
    
    /**
     * Connexion vers la table des {@linkplain Reference reference}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ReferenceTable references;
    
    /**
     * Connexion vers la table des {@linkplain DataBlockDefinition dataBlockDefinition}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private DataBlockTable dataBlocks;
    
    /**
     * Construit une table des resultats.
     *
     * @param  database Connexion vers la base de données.
     */
    public AnyResultTable(final Database database) {
        this(new AnyResultQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private AnyResultTable(final AnyResultQuery query) {
        super(query);
        setIdentifierParameters(query.byIdResult, null);
    }
   
    /**
     * Construit une reference pour l'enregistrement courant.
     */
    protected AnyResultEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
         final AnyResultQuery query = (AnyResultQuery) super.query;
          
         if(references == null) {
             references = getDatabase().getTable(ReferenceTable.class);
         }
         Reference ref = references.getEntry(results.getString(indexOf(query.reference)));
         
         if(dataBlocks == null) {
             dataBlocks = getDatabase().getTable(DataBlockTable.class);
         }
         DataBlockEntry dataBlock = dataBlocks.getEntry(results.getString(indexOf(query.dataBlock)));
         
         return new AnyResultEntry(results.getInt(indexOf(query.idResult)), ref, dataBlock);
    }
    
}
