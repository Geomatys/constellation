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
package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

// Sicade dependencies
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.coverage.model.Distribution;
import net.sicade.observation.MeasurementEntry;

// OpenGis dependencies
import org.opengis.observation.Measure;
import org.opengis.observation.Measurement;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalObject;
import org.opengis.metadata.MetaData;
import org.opengis.metadata.quality.Element;


/**
 * Connexion vers la table des {@linkplain Measurement mesures}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @see MergedMeasurementTable
 * @see net.sicade.observation.coverage.MeasurementTableFiller
 */
@Deprecated
public class MeasurementTable extends ObservationTable<Measurement> {
    
    /**
     * Construit une nouvelle connexion vers la table des mesures.
     */
    public MeasurementTable(final Database database) {
         super(new MeasurementQuery(database));
    }
        
    
    /**
     * Construit une mesure pour l'enregistrement courant
     */
    protected Measurement createEntry(final ResultSet result) throws SQLException {
        
                final Measure         result;
                final TemporalObject  samplingTime;
                final MetaData        observationMetadata;
                final String          resultDefinition;
                final TemporalObject  procedureTime;
                final Object          procedureParameter;
                
                return new MeasurementEntry(definition, featureOfInterest, observedProperty, procedure, distribution, quality,
                result, samplingTime, observationMetadata, resultDefinition, procedureTime, procedureParameter);
    }
    
}
