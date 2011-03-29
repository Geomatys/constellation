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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;

import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;

/**
 *
 * @author legal
 */
public class EnvelopeTable extends SingletonTable<EnvelopeType> implements Cloneable {

    
    /**
     * Construit une table des envelope.
     *
     * @param  database Connexion vers la base de données.
     */
    public EnvelopeTable(final Database database) {
        this(new EnvelopeQuery(database));
    }

    /**
     * Initialise l'identifiant de la table.
     */
    private EnvelopeTable(final EnvelopeQuery query) {
        super(query, query.byId);
    }

    /**
     * Creates a new instance having the same configuration than the given table.
     * This is a copy constructor used for obtaining a new instance to be used
     * concurrently with the original instance.
     *
     * @param table The table to use as a template.
     */
    private EnvelopeTable(final EnvelopeTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected EnvelopeTable clone() {
        return new EnvelopeTable(this);
    }
    
    /**
     * Cree une nouvelle envellope a partir de la base de donnees
     * 
     * @param results un resultSet contenant une ligne de la table des envelopes.
     * 
     * @return
     * @throws org.constellation.catalog.CatalogException
     * @throws java.sql.SQLException
     */
    @Override
    protected EnvelopeType createEntry(final LocalCache loc, ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final EnvelopeQuery query = (EnvelopeQuery) super.query;
        //on lit le premier point
        List<Double> value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf(query.lowerCornerX)));
        value.add(results.getDouble(indexOf(query.lowerCornerY)));
        final DirectPositionType lc = new DirectPositionType(value);
        
        //puis le second
        value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf(query.upperCornerX)));
        value.add(results.getDouble(indexOf(query.upperCornerY)));
        final DirectPositionType uc = new DirectPositionType(value);
        return new EnvelopeType(results.getString(indexOf(query.id)),
                                 lc, uc,
                                 results.getString(indexOf(query.srsName)));
    }
    
    /**
     * 
     */
     /**
     * Retourne un nouvel identifier (ou l'identifier de l'offering passée en parametre si non-null)
     * et enregistre la nouvelle offering dans la base de donnée.
     *
     * @param off l'ofeering a inserer dans la base de donnée.
     */
    public String getIdentifier(final EnvelopeType envelope) throws SQLException, CatalogException {
        final EnvelopeQuery query = (EnvelopeQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (envelope.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.id), envelope.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return envelope.getId();
                    } else {
                        id = envelope.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {

                    id = searchFreeIdentifier(lc, "envelope:");
                }
                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.id), id);
                if (envelope.getSrsName() != null) {
                    statement.statement.setString(indexOf(query.srsName), envelope.getSrsName());
                } else {
                    statement.statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
                }

                if (envelope.getAxisLabels() != null && envelope.getAxisLabels().size() != 0) {
                    log("getIdentifier", new LogRecord(Level.WARNING, "Axis Labels are not yet recordable"));
                }

                if (envelope.getLowerCorner() != null && envelope.getLowerCorner().getValue().size() == 2) {
                    statement.statement.setDouble(indexOf(query.lowerCornerX), envelope.getLowerCorner().getValue().get(0));
                    statement.statement.setDouble(indexOf(query.lowerCornerY), envelope.getLowerCorner().getValue().get(1));
                } else {
                    log("getIdentifier", new LogRecord(Level.WARNING, "lowerCorner null ou malformed"));
                    statement.statement.setNull(indexOf(query.lowerCornerX), java.sql.Types.DOUBLE);
                    statement.statement.setNull(indexOf(query.lowerCornerY), java.sql.Types.DOUBLE);
                }

                if (envelope.getUpperCorner() != null && envelope.getUpperCorner().getValue().size() == 2) {
                    statement.statement.setDouble(indexOf(query.upperCornerX), envelope.getUpperCorner().getValue().get(0));
                    statement.statement.setDouble(indexOf(query.upperCornerY), envelope.getUpperCorner().getValue().get(1));
                } else {
                    log("getIdentifier", new LogRecord(Level.WARNING, "upperCorner null ou malformed"));
                    statement.statement.setNull(indexOf(query.upperCornerX), java.sql.Types.DOUBLE);
                    statement.statement.setNull(indexOf(query.upperCornerY), java.sql.Types.DOUBLE);
                }
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
