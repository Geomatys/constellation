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
package org.constellation.observation;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.constellation.gml.v311.UnitOfMeasureTable;
import org.geotoolkit.gml.xml.v311.UnitOfMeasureEntry;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.observation.xml.v100.MeasureType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class MeasureTable extends SingletonTable<MeasureType> {
    
    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private UnitOfMeasureTable uoms;
    
    /**
     * Construit une table des resultats de mesure.
     * 
     * @param  database Connexion vers la base de données.
     */
    public MeasureTable(final Database database) {
         this(new MeasureQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private MeasureTable(final MeasureQuery query) {
        super(query, query.byName);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private MeasureTable(final MeasureTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected MeasureTable clone() {
        return new MeasureTable(this);
    }
    

    /**
     * Construit un resultat de mesure pour l'enregistrement courant.
     */
    @Override
    protected MeasureType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException {
        final MeasureQuery query = (MeasureQuery) super.query;
        if(uoms == null) {
            uoms =  getDatabase().getTable(UnitOfMeasureTable.class);
        }
        final UnitOfMeasureEntry uom = uoms.getEntry(results.getString(indexOf(query.uom)));
        return new MeasureType(results.getString(indexOf(query.name   )),
                                uom,
                                results.getFloat(indexOf(query.value)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du resultat de mesure passée en parametre si non-null)
     * et enregistre le nouveau resultat de mesure dans la base de donnée si il n'y est pas deja.
     *
     * @param meas le resultat de mesure a inserer dans la base de donnée.
     */
    public String getIdentifier(final MeasureType meas) throws SQLException, CatalogException {
        final MeasureQuery query  = (MeasureQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
        transactionBegin(lc);
            try {
                if (meas.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.name), meas.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return meas.getName();
                    } else {
                        id = meas.getName();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "mesure");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);

                statement.statement.setString(indexOf(query.name), id);
                if (uoms == null) {
                    uoms =  getDatabase().getTable(UnitOfMeasureTable.class);
                }
                if (meas.getUom() != null) {
                    statement.statement.setString(indexOf(query.uom), uoms.getIdentifier(meas.getUom()));
                } else {
                    statement.statement.setNull(indexOf(query.uom), java.sql.Types.VARCHAR);
                }
                statement.statement.setDouble(indexOf(query.value), meas.getValue());
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
