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
import net.sicade.swe.TextBlockEntry;

/**
 *  Connexion vers la table des {@linkplain TextBlock textBlock}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockTable extends SingletonTable<TextBlock>{
    
    /**
     * Construit une table des text Block encodage.
     *
     * @param  database Connexion vers la base de données.
     */
    public TextBlockTable(final Database database) {
           super(new TextBlockQuery(database)); 
    }

    protected TextBlock createEntry(final ResultSet results) throws CatalogException, SQLException {
        final TextBlockQuery query = (TextBlockQuery) super.query;
        return new TextBlockEntry(results.getString(indexOf(query.id )),
                                  results.getString(indexOf(query.tokenSeparator )),
                                  results.getString(indexOf(query.blockSeparator )),
                                  results.getString(indexOf(query.decimalSeparator)).charAt(0));
    }
    
}
