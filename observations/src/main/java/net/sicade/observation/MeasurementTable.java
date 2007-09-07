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

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.observation.sql.*;
import org.opengis.observation.Measurement;

/**
 * Connexion vers la table des {@linkplain Measurement mesures}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 *
 * @see MergedMeasurementTable
 * @see net.sicade.observation.coverage.MeasurementTableFiller
 */
@Deprecated
public class MeasurementTable extends ObservationTable<Measurement> {
    
    /**
     * Connexion vers la table des {@linkplain Measure measure}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected MeasureTable measures;
    
    /**
     * Construit une nouvelle connexion vers la table des mesures.
     */
    public MeasurementTable(final Database database) {
         super(new MeasurementQuery(database));
    }
        
    
    /**
     * Construit une mesure pour l'enregistrement courant
     */
    protected Measurement createEntry(final ResultSet result) throws SQLException, CatalogException {
        
                 final MeasurementQuery query = (MeasurementQuery) super.query;
                
                return new MeasurementEntry(result.getString(indexOf(query.name   )),
                                            result.getString(indexOf(query.description)),
                                            stations.getEntry(result.getString(indexOf(query.featureOfInterest))),
                                            phenomenons.getEntry(result.getString(indexOf(query.observedProperty))), 
                                            procedures.getEntry(result.getString(indexOf(query.procedure))),
                                            distributions.getEntry(result.getString(indexOf(query.distribution))),
                                            //manque quality
                                            measures.getEntry(result.getString(indexOf(query.result))),
                                            new TemporalObjectEntry(result.getDate(indexOf(query.samplingTimeBegin)),
                                                                    result.getDate(indexOf(query.samplingTimeEnd))),
                                            result.getString(indexOf(query.resultDefinition)));
    }
    
}
