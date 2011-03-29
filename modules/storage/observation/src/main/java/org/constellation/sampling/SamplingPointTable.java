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
package org.constellation.sampling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;

/**
 *SamplingPointTable.java
 *
 * @author Guilhem Legal
 */
public class SamplingPointTable extends SingletonTable<SamplingPointType> implements Cloneable {
    
    /** Creates a new instance of SamplingPointTable */
    public SamplingPointTable(final Database database) {
        this(new SamplingPointQuery(database));
    }
    
     /**
     * Initialise l'identifiant de la table.
     */
    private SamplingPointTable(final SamplingPointQuery query) {
        super(query, query.byIdentifier);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private SamplingPointTable(final SamplingPointTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected SamplingPointTable clone() {
        return new SamplingPointTable(this);
    }

    /**
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * premiére série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    @Override
    protected SamplingPointType createEntry(final LocalCache lc, final ResultSet result, Comparable<?> identifier) throws CatalogException, SQLException {
        final SamplingPointQuery query = (SamplingPointQuery) super.query;
        
        final List<Double> value = new ArrayList<Double>();
        value.add(result.getDouble(indexOf(query.positionValueX)));
        value.add(result.getDouble(indexOf(query.positionValueY)));
                
        final PointType p = new PointType(result.getString(indexOf(query.pointIdentifier)),
                                          new DirectPositionType(result.getString(indexOf(query.srsName)),
                                          result.getInt(indexOf(query.srsDimension)),
                                          value));
        // TODO result.getString(indexOf(query.sampledFeature)
        return new SamplingPointType( result.getString(indexOf(query.identifier)),
                                       result.getString(indexOf(query.name)),
                                       result.getString(indexOf(query.description)),
                                       new FeaturePropertyType(result.getString(indexOf(query.sampledFeature))),
                                       new PointPropertyType(p));
        
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la station passée en parametre si non-null)
     * et enregistre la nouvelle station dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final SamplingPointType station) throws SQLException, CatalogException {
        final SamplingPointQuery query  = (SamplingPointQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                // the station recived by xml have no ID so we use the name as a second primary key
                if (station.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.LIST);
                    statement.statement.setString(indexOf(query.byName), station.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        id = result.getString("id");
                        station.setId(id);
                        result.close();
                        release(lc, statement);
                        return id;
                    } else {
                        if (station.getId() != null) {
                            id = station.getId();
                        } else {
                           id = searchFreeIdentifier(lc, "station");
                        }
                    }
                    result.close();
                    release(lc, statement);
                    
                } else {
                   throw new CatalogException("the station must have a name");
                }
                station.setId(id);
                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.identifier), id);

                if (station.getDescription() != null) {
                    statement.statement.setString(indexOf(query.description), station.getDescription());
                } else {
                    statement.statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
                }

                statement.statement.setString(indexOf(query.name), station.getName());
                final Iterator<FeaturePropertyType> i = station.getSampledFeatures().iterator();
                if (i.hasNext()) {
                    final FeaturePropertyType fp = i.next();
                    statement.statement.setString(indexOf(query.sampledFeature), (String)fp.getHref());
                } else {
                    statement.statement.setNull(indexOf(query.sampledFeature), java.sql.Types.VARCHAR);
                }

                if( station.getPosition() != null ) {
                    statement.statement.setString(indexOf(query.pointIdentifier), station.getPosition().getId());
                    statement.statement.setString(indexOf(query.srsName), station.getPosition().getPos().getSrsName());
                    statement.statement.setDouble(indexOf(query.positionValueX), station.getPosition().getPos().getValue().get(0));
                    statement.statement.setDouble(indexOf(query.positionValueY), station.getPosition().getPos().getValue().get(1));
                    statement.statement.setInt(indexOf(query.srsDimension), station.getPosition().getPos().getDimension());
                } else {
                    statement.statement.setNull(indexOf(query.pointIdentifier), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.srsName), java.sql.Types.VARCHAR);
                    statement.statement.setNull(indexOf(query.positionValueX), java.sql.Types.DOUBLE);
                    statement.statement.setNull(indexOf(query.positionValueY), java.sql.Types.DOUBLE);
                    statement.statement.setNull(indexOf(query.srsDimension), java.sql.Types.INTEGER);
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
