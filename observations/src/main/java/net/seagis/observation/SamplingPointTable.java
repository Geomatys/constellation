/*
 * Sicade - Systémes intégrés de connaissances pour l'aide é la décision en environnement
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

package net.seagis.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.gml32.DirectPositionType;
import net.seagis.gml32.PointType;

/**
 *SamplingPointTable.java
 *
 * @author Guilhem Legal
 */
public class SamplingPointTable extends SingletonTable<SamplingPointEntry> {
    
    /** Creates a new instance of SamplingPointTable */
    public SamplingPointTable(final Database database) {
        this(new SamplingPointQuery(database));
    }
    
     /**
     * Initialise l'identifiant de la table.
     */
    private SamplingPointTable(final SamplingPointQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
    }
    
    /**
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * premiére série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    @Override
    protected SamplingPointEntry createEntry(final ResultSet result) throws CatalogException, SQLException {
        final SamplingPointQuery query = (SamplingPointQuery) super.query;
        
        List<Double> value = new ArrayList<Double>();
        value.add(result.getDouble(indexOf((query.positionValueX))));
        value.add(result.getDouble(indexOf((query.positionValueY))));
                
        PointType p = new PointType(result.getString(indexOf(query.pointIdentifier)),
                                    new DirectPositionType(result.getString(indexOf(query.srsName)),
                                    result.getInt(indexOf(query.srsDimension)),
                                    value));
                            
        return new SamplingPointEntry( result.getString(indexOf(query.identifier)),
                                       result.getString(indexOf(query.name)),
                                       result.getString(indexOf(query.description)),
                                       result.getString(indexOf(query.sampledFeature)),
                                       p);
        
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la station passée en parametre si non-null)
     * et enregistre la nouvelle station dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final SamplingPointEntry station) throws SQLException, CatalogException {
        final SamplingPointQuery query  = (SamplingPointQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (station.getId() != null) {
                PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.identifier), station.getId());
                ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return station.getId();
                } else {
                    id = station.getId(); 
                }
            } else {
                id = searchFreeIdentifier("station");
            }
        
            PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.identifier), id);
        
            if (station.getDescription() != null) {
                statement.setString(indexOf(query.description), station.getDescription());
            } else {
                statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
            }
        
            statement.setString(indexOf(query.name), station.getName());
            Iterator i = station.getSampledFeatures().iterator();
            statement.setString(indexOf(query.sampledFeature), (String)i.next());
        
            if( station.getPosition() != null ) {
                statement.setString(indexOf(query.pointIdentifier), station.getPosition().getId());
                statement.setString(indexOf(query.srsName), station.getPosition().getPos().getSrsName());
                statement.setDouble(indexOf(query.positionValueX), station.getPosition().getPos().getValue().get(0));
                statement.setDouble(indexOf(query.positionValueY), station.getPosition().getPos().getValue().get(1));
                statement.setInt(indexOf(query.srsDimension), station.getPosition().getPos().getSrsDimension());
            } else {
                statement.setNull(indexOf(query.pointIdentifier), java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
                statement.setNull(indexOf(query.positionValueX), java.sql.Types.DOUBLE);
                statement.setNull(indexOf(query.positionValueY), java.sql.Types.DOUBLE);
                statement.setNull(indexOf(query.srsDimension), java.sql.Types.INTEGER);
            }
            updateSingleton(statement); 
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
}
