/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.gml.v311;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.gml.xml.v311.UnitOfMeasureEntry;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;

/**
 * Connexion vers la table des {@linkplain UnitOfMeasure unit of measure}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class UnitOfMeasureTable extends SingletonTable<UnitOfMeasureEntry> {
    
   /**
    * Construit une table des unites de mesure.
    *
    * @param  database Connexion vers la base de données.
    */
    public UnitOfMeasureTable(final Database database) {
        this(new UnitOfMeasureQuery(database)); 
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private UnitOfMeasureTable(final UnitOfMeasureQuery query) {
        super(query, query.byId);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private UnitOfMeasureTable(final UnitOfMeasureTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected UnitOfMeasureTable clone() {
        return new UnitOfMeasureTable(this);
    }

    /**
     * Crée une entrée pour l'untié de mesure courante.
     */
    @Override
    protected UnitOfMeasureEntry createEntry(final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
          final UnitOfMeasureQuery query = (UnitOfMeasureQuery) super.query;
          return new UnitOfMeasureEntry(results.getString(indexOf(query.id )),
                                         results.getString(indexOf(query.name )),
                                         results.getString(indexOf(query.quantityType )),
                                         results.getString(indexOf(query.unitSystem )));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de l'unité passée en parametre si non-null)
     * et enregistre le nouveau unité dans la base de donnée si il n'y est pas deja.
     *
     * @param proc l' unité de mesure a inserer dans la base de donnée.
     */
    public String getIdentifier(final UnitOfMeasureEntry uom) throws SQLException, CatalogException {
        final UnitOfMeasureQuery query  = (UnitOfMeasureQuery) super.query;
        String id;
        boolean success = false;
        synchronized (getLock()) {
            transactionBegin();
            try {
                if (uom.getId() != null) {
                    final Stmt statement = getStatement(QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.id), uom.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(statement);
                        return uom.getId();
                    } else {
                        id = uom.getId();
                    }
                    result.close();
                    release(statement);
                } else {
                    id = searchFreeIdentifier("uom");
                }

                final Stmt statement = getStatement(QueryType.INSERT);

                statement.statement.setString(indexOf(query.id),           id);
                statement.statement.setString(indexOf(query.name),         uom.getName());
                statement.statement.setString(indexOf(query.quantityType), uom.getQuantityType());
                statement.statement.setString(indexOf(query.unitSystem),   uom.getUnitsSystem());
                updateSingleton(statement.statement);
                release(statement);
                success = true;
            } finally {
                transactionEnd(success);
            }
        }
        return id;
    }
    
}
