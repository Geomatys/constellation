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
package net.seagis.swe;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;

/**
 *  Connexion vers la table des {@linkplain TextBlock textBlock}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockTable extends SingletonTable<TextBlockEntry>{
    
    /**
     * Construit une table des text Block encodage.
     *
     * @param  database Connexion vers la base de données.
     */
    public TextBlockTable(final Database database) {
           this(new TextBlockQuery(database)); 
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private TextBlockTable(final TextBlockQuery query) {
        super(query);
        setIdentifierParameters(query.byId, null);
    }

    /**
     * Crée un nouveau encodage a partir de la base de donnée.
     *
     * @param results un resultSet contenant un tuple de la table de encodage textuel.
     */
    protected TextBlockEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final TextBlockQuery query = (TextBlockQuery) super.query;
        return new TextBlockEntry(results.getString(indexOf(query.id )),
                                  results.getString(indexOf(query.tokenSeparator )),
                                  results.getString(indexOf(query.blockSeparator )),
                                  results.getString(indexOf(query.decimalSeparator)).charAt(0));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du textBlock passée en parametre si non-null)
     * et enregistre le nouveau TextBlock dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le datablockDefinition a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final TextBlockEntry textbloc) throws SQLException, CatalogException {
        final TextBlockQuery query  = (TextBlockQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (textbloc.getId() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.id), textbloc.getId());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return textbloc.getId();
                } else {
                    id = textbloc.getId();
                }
            } else {
                id = searchFreeIdentifier("textblock");
            }
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.id), id);
            statement.setString(indexOf(query.decimalSeparator), textbloc.getDecimalSeparator() + "");
            statement.setString(indexOf(query.blockSeparator), textbloc.getBlockSeparator());
            statement.setString(indexOf(query.tokenSeparator), textbloc.getTokenSeparator());
        
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
