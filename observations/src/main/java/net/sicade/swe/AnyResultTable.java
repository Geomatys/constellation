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
import net.sicade.gml.ReferenceEntry;
import net.sicade.gml.ReferenceTable;

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
         ReferenceEntry ref = references.getEntry(results.getString(indexOf(query.reference)));
         
         return new AnyResultEntry(results.getString(indexOf(query.idResult)), ref, 
                                   results.getString(indexOf(query.dataBlock)));
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du resultat passée en parametre si non-null)
     * et enregistre le nouveau resultat dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final Object result) throws SQLException, CatalogException {
        final AnyResultQuery query = (AnyResultQuery) super.query;
        
        String id;
        if (result instanceof String) {
            PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
            statement.setString(indexOf(query.dataBlock), (String)result);
            statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
            ResultSet results = statement.executeQuery();
            if(results.next())
                return results.getString(1);
            else
                id = searchFreeIdentifier("idresult");
        } else if (result instanceof ReferenceEntry) {
            PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
            statement.setString(indexOf(query.reference), ((ReferenceEntry)result).getId());
            statement.setNull(indexOf(query.dataBlock), java.sql.Types.VARCHAR);
            ResultSet results = statement.executeQuery();
            if(results.next())
                return results.getString(1);
            else
                id = searchFreeIdentifier("idresult");
        } else {
            throw new CatalogException(" ce type de resultat n'est pas accepté");
        }
        
        PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString(indexOf(query.idResult), id);
        if (result instanceof String) {
            statement.setString(indexOf(query.dataBlock), (String)result);
            statement.setNull(indexOf(query.reference), java.sql.Types.VARCHAR);
        } else {
            if (result instanceof ReferenceEntry) {
                ReferenceEntry ref = (ReferenceEntry) result;
                String idRef;
                
                if(references == null) {
                    references = getDatabase().getTable(ReferenceTable.class);
                }
                idRef = references.getIdentifier(ref);
                
                statement.setString(indexOf(query.reference), idRef);
                statement.setNull(indexOf(query.dataBlock), java.sql.Types.VARCHAR);
            } else {
                throw new CatalogException(" ce type de resultat n'est pas accepté");
            }
        }
        insertSingleton(statement);   
        return id;
    }
    
}
