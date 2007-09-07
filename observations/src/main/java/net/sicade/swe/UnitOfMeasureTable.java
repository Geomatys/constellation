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
import net.sicade.swe.UnitOfMeasureEntry;

/**
 * Connexion vers la table des {@linkplain UnitOfMeasure unit of measure}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class UnitOfMeasureTable extends SingletonTable<BaseUnit>{
    
   /**
    * Construit une table des unites de mesure.
    *
    * @param  database Connexion vers la base de données.
    */
    public UnitOfMeasureTable(final Database database) {
        super(new UnitOfMeasureQuery(database)); 
    }

    protected BaseUnit createEntry(final ResultSet results) throws CatalogException, SQLException {
          final UnitOfMeasureQuery query = (UnitOfMeasureQuery) super.query;
          return new UnitOfMeasureEntry(results.getString(indexOf(query.id )),
                                         results.getString(indexOf(query.name )),
                                         results.getString(indexOf(query.quantityType )),
                                         results.getString(indexOf(query.unitSystem )));
    }
    
}
