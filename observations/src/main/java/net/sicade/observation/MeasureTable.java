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
package net.sicade.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.gml.UnitOfMeasureEntry;
import net.sicade.gml.UnitOfMeasureTable;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class MeasureTable extends SingletonTable<MeasureEntry> {
    
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
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    

    /**
     * Construit un resultat de mesure pour l'enregistrement courant.
     */
    protected MeasureEntry createEntry(final ResultSet results) throws SQLException, CatalogException {
        final MeasureQuery query = (MeasureQuery) super.query;
        if(uoms == null) {
            uoms =  getDatabase().getTable(UnitOfMeasureTable.class);
        }
        UnitOfMeasureEntry uom = uoms.getEntry(results.getString(indexOf(query.uom)));
        return new MeasureEntry(results.getString(indexOf(query.name   )),
                                uom,
                                results.getFloat(indexOf(query.value)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du resultat de mesure passée en parametre si non-null)
     * et enregistre le nouveau resultat de mesure dans la base de donnée si il n'y est pas deja.
     *
     * @param meas le resultat de mesure a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final MeasureEntry meas) throws SQLException, CatalogException {
        final MeasureQuery query  = (MeasureQuery) super.query;
        String id;
        if (meas.getName() != null) {
            PreparedStatement statement = getStatement(QueryType.EXISTS);
            statement.setString(indexOf(query.name), meas.getName());
            ResultSet result = statement.executeQuery();
            if(result.next())
                return meas.getName();
            else
                id = meas.getName();
        } else {
            id = searchFreeIdentifier("mesure");
        }
        
        PreparedStatement statement = getStatement(QueryType.INSERT);
        
        statement.setString(indexOf(query.name), id);
        if (uoms == null) {
             uoms =  getDatabase().getTable(UnitOfMeasureTable.class);
        }
        statement.setString(indexOf(query.uom), uoms.getIdentifier(meas.getUom()));
        statement.setDouble(indexOf(query.value), meas.getValue());
        insertSingleton(statement);
        return id;
    }
    
}
