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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;

/**
 *
 * @author legal
 */
public class EnvelopeTable extends SingletonTable<EnvelopeEntry> {

    
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
        super(query);
        setIdentifierParameters(query.byId, null);
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
    protected EnvelopeEntry createEntry(ResultSet results) throws CatalogException, SQLException {
        final EnvelopeQuery query = (EnvelopeQuery) super.query;
        //on lit le premier point
        List<Double> value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf((query.lowerCornerX))));
        value.add(results.getDouble(indexOf((query.lowerCornerY))));
        DirectPositionType lc = new DirectPositionType(null,-1, null, value);
        
        //puis le second
        value = new ArrayList<Double>();
        value.add(results.getDouble(indexOf((query.upperCornerX))));
        value.add(results.getDouble(indexOf((query.upperCornerY))));
        DirectPositionType uc = new DirectPositionType(null,-1, null, value);
        return new EnvelopeEntry(results.getString(indexOf(query.id)),
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
    public synchronized String getIdentifier(final EnvelopeEntry envelope) throws SQLException, CatalogException {
        final EnvelopeQuery query = (EnvelopeQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (envelope.getName() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.id), envelope.getName());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return envelope.getName();
                } else {
                    id = envelope.getName();
                }
            } else {
                id = searchFreeIdentifier("envelope:");
            }
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.id), id);
            if (envelope.getSrsName() != null) {
                statement.setString(indexOf(query.srsName), envelope.getSrsName());
            } else {
                statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
            }
        
            if (envelope.getAxisLabels() != null && envelope.getAxisLabels().size() != 0) {
                System.out.println("Axis Labels are not yet recordable");
            }
        
            if (envelope.getLowerCorner() != null && envelope.getLowerCorner().getValue().size() == 2) {
                statement.setDouble(indexOf(query.lowerCornerX), envelope.getLowerCorner().getValue().get(0));
                statement.setDouble(indexOf(query.lowerCornerY), envelope.getLowerCorner().getValue().get(1));
            } else {
                System.out.println("lowerCorner null ou mal forme");
                statement.setNull(indexOf(query.lowerCornerX), java.sql.Types.DOUBLE);
                statement.setNull(indexOf(query.lowerCornerY), java.sql.Types.DOUBLE);
            }
        
            if (envelope.getUpperCorner() != null && envelope.getUpperCorner().getValue().size() == 2) {
                statement.setDouble(indexOf(query.upperCornerX), envelope.getUpperCorner().getValue().get(0));
                statement.setDouble(indexOf(query.upperCornerY), envelope.getUpperCorner().getValue().get(1));
            } else {
                System.out.println("upperCorner null ou mal forme");
                statement.setNull(indexOf(query.upperCornerX), java.sql.Types.DOUBLE);
                statement.setNull(indexOf(query.upperCornerY), java.sql.Types.DOUBLE);
            }
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    

}
