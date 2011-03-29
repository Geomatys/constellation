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
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.TextBlockType;

/**
 *  Connexion vers la table des {@linkplain TextBlock textBlock}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockTable extends SingletonTable<TextBlockType> implements Cloneable {
    
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
        super(query, query.byId);
    }

     /**
     * Construit une nouvelle table non partagée
     */
    private TextBlockTable(final TextBlockTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected TextBlockTable clone() {
        return new TextBlockTable(this);
    }

    /**
     * Crée un nouveau encodage a partir de la base de donnée.
     *
     * @param results un resultSet contenant un tuple de la table de encodage textuel.
     */
    @Override
    protected TextBlockType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final TextBlockQuery localQuery = (TextBlockQuery) super.query;
        return new TextBlockType(results.getString(indexOf(localQuery.id )),
                                  results.getString(indexOf(localQuery.tokenSeparator )),
                                  results.getString(indexOf(localQuery.blockSeparator )),
                                  results.getString(indexOf(localQuery.decimalSeparator)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du textBlock passée en parametre si non-null)
     * et enregistre le nouveau TextBlock dans la base de donnée si il n'y est pas deja.
     *
     * @param databloc le datablockDefinition a inserer dans la base de donnée.
     */
    public String getIdentifier(final TextBlockType textbloc) throws SQLException, CatalogException {
        final TextBlockQuery localQuery  = (TextBlockQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (textbloc.getId() == null) {
                    final Stmt statement = getStatement(lc, QueryType.LIST);
                    statement.statement.setString(indexOf(localQuery.byBlockSeparator), textbloc.getBlockSeparator());
                    statement.statement.setString(indexOf(localQuery.byDecimalSeparator), textbloc.getDecimalSeparator());
                    statement.statement.setString(indexOf(localQuery.byTokenSeparator), textbloc.getTokenSeparator());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return result.getString("id_encoding");
                    } else {
                        id = searchFreeIdentifier(lc, "textblock");
                    }
                    result.close();
                    release(lc, statement);
                //if the id is not null we assume that the textBlock is already recorded int the database
                } else {
                    id = textbloc.getId();
                    return id;
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(localQuery.id), id);
                statement.statement.setString(indexOf(localQuery.decimalSeparator), textbloc.getDecimalSeparator());
                statement.statement.setString(indexOf(localQuery.blockSeparator)  , textbloc.getBlockSeparator());
                statement.statement.setString(indexOf(localQuery.tokenSeparator)  , textbloc.getTokenSeparator());

                updateSingleton(statement.statement);
                release(lc, statement);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
