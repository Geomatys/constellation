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

import javax.units.Unit;

// Sicade dependencies
import net.sicade.coverage.model.Distribution;


// openGis dependencies
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;
import org.opengis.observation.Measurement;
import org.opengis.metadata.quality.Element;
import org.opengis.observation.Measure;
import org.opengis.temporal.TemporalObject;
import org.opengis.metadata.MetaData;
import org.opengis.observation.sampling.SamplingFeature;

/**
 * Implémentation d'une entrée représentant une {@linkplain Measurement mesure}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class MeasurementEntry extends ObservationEntry implements Measurement {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 6700527485309897974L;
    
    
    /**
     * le resultat de la mesure
     */
    private Measure result;
    
    
    /**
     * Crée une nouvelle mesure.
     *
     * @param station     La station d'observation (par exemple une position de pêche).
     * @param observable  Ce que l'on observe (température, quantité pêchée, <cite>etc.</cite>).
     * @param value       La valeur mesurée.
     * @param error       Estimation de l'erreur sur la valeur mesurée, ou {@link Float#NaN NaN}
     *                    si l'erreur est inconnue ou ne s'applique pas.
     */
    protected MeasurementEntry(final SamplingFeature station,
            final Phenomenon      observedProperty,
            final Process         procedure,
            final Distribution    distribution,
            final Element         quality,
            final Measure         result,
            final TemporalObject  samplingTime,
            final MetaData        observationMetadata,
            final String          resultDefinition,
            final TemporalObject  procedureTime,
            final Object          procedureParameter) {
        super(station, observedProperty, procedure, distribution, quality, result,
                samplingTime, observationMetadata, resultDefinition, procedureTime, procedureParameter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createName() {
        final StringBuilder name = new StringBuilder(super.createName()).append(" = ");
        
        return name.toString();
    }
    
    
    
    
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final MeasurementEntry that = (MeasurementEntry) object;
            return this.getResult().equals(that.getResult());
        }
        return false;
    }
    
    public Measure getResult() {
        return result;
    }
}
