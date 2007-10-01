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

package net.sicade.observation;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import org.opengis.observation.sampling.SamplingPoint;

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
    protected SamplingPointEntry createEntry(final ResultSet result) throws CatalogException, SQLException {
        final SamplingPointQuery query = (SamplingPointQuery) super.query;
        
        PointType p = new PointType(result.getString(indexOf(query.pointIdentifier)),
                                    new DirectPositionType(result.getString(indexOf(query.srsName)),
                                    new BigInteger(result.getString(indexOf(query.srsDimension))),
                                    result.getDouble(indexOf(query.positionValue))));
                            
        return new SamplingPointEntry( result.getString(indexOf(query.identifier)),
                                       result.getString(indexOf(query.name)),
                                       result.getString(indexOf(query.description)),
                                       result.getString(indexOf(query.sampledFeature)),
                                       p);
        
    }
    
}
