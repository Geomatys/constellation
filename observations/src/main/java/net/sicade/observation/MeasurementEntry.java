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

import javax.units.Unit;
import net.sicade.coverage.model.DistributionEntry;
import org.opengis.observation.Measurement;
import org.opengis.observation.Measure;

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
    private MeasureEntry result;
    
    
    /**
     * Crée une nouvelle mesure.
     *
     * @param station           La station d'observation (par exemple une position de pêche).
     * @param observedProperty  Ce que l'on observe (température, quantité pêchée, <cite>etc.</cite>).
     * @param process           La procedure effectuée sur cette operation.
     * @param distribution
     * @param quality
     * @param result            Le resultat de l'observation, ici une measure.
     * @param samplingTime
     * @param observationMetadata
     * @param resultDefinition
     * @param procedureTime
     * @param procedureParameter
     */
    public MeasurementEntry(final String name,
            final String definition,
            final SamplingFeatureEntry station,
            final PhenomenonEntry      observedProperty,
            final ProcessEntry         procedure,
            final DistributionEntry    distribution,
            final ElementEntry         quality,
            final MeasureEntry         result,
            final TemporalObjectEntry  samplingTime,
            final MetaDataEntry        observationMetadata,
            final String               resultDefinition,
            final TemporalObjectEntry  procedureTime,
            final Object               procedureParameter) {
        super(name, definition, station, observedProperty, procedure, distribution, quality, result,
                samplingTime, observationMetadata, resultDefinition, procedureTime, procedureParameter);
    }
    
    /**
     * Crée une nouvelle mesure  reduite adapté a BRGM.
     *
     * @param station     La station d'observation (par exemple une position de pêche).
     * @param observable  Ce que l'on observe (température, quantité pêchée, <cite>etc.</cite>).
     * @param value       La valeur mesurée.
     * @param error       Estimation de l'erreur sur la valeur mesurée, ou {@link Float#NaN NaN}
     *                    si l'erreur est inconnue ou ne s'applique pas.
     */
    public MeasurementEntry(final String name,
            final String definition,
            final SamplingFeatureEntry station,
            final PhenomenonEntry      observedProperty,
            final ProcessEntry         procedure,
            final DistributionEntry    distribution,
            //final ElementEntry         quality,
            final MeasureEntry         result,
            final TemporalObjectEntry  samplingTime,
            final String               resultDefinition) {
        super(name, definition, station, observedProperty, procedure, distribution, result,
                samplingTime, resultDefinition);
        
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createName() {
        final StringBuilder name = new StringBuilder(super.createName()).append(" = ");
        
        return name.toString();
    }
    
    public Measure getResult() {
        return result;
    }
    
    
    
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final MeasurementEntry that = (MeasurementEntry) object;
            return this.getResult().equals(that.getResult());
        }
        return false;
    }
    
    
}
