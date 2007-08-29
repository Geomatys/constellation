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

import net.sicade.catalog.Entry;
import net.sicade.coverage.model.Distribution;
import net.sicade.observation.SamplingFeature;
import net.sicade.observation.Observation;
import org.opengis.observation.Process;
import org.geotools.resources.Utilities;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.observation.Phenomenon;


/**
 * Implémentation d'une entrée représentant une {@linkplain Observation observation}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class ObservationEntry extends Entry implements Observation {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3269639171560208276L;

    /**
     * La station à laquelle a été pris cet échantillon.
     */
    private final SamplingFeature featureOfInterest;
    
    /**
     * Référence vers le {@linkplain Phenomenon phénomène} observé.
     */
    private final Phenomenon observedProperty;

    /**
     * Référence vers la {@linkplain Procedure procédure} associée à cet observable.
     */
    private final Process procedure;
    
     /**
     * Référence vers la {@linkplain Distribution distribution} associée à cet observable.
     */
    private final Distribution distribution;
    
    /**
     * La qualité de la donnée. Peut être nul si cette information n'est pas disponible.
     */
    private final DataQuality quality;

    /**
     * Construit une observation.
     * 
     * @param featureOfInterest La station d'observation (par exemple une position de pêche).
     * @param observedProperty  Le phénomène observé.
     * @param procedure         La procédure associée.
     * @param quality    La qualité de la donnée, ou {@code null} si inconnue.
     */
    protected ObservationEntry(final SamplingFeature featureOfInterest, 
                               final Phenomenon      observedProperty,
                               final Process         procedure,
                               final Distribution    distribution,
                               final DataQuality  quality) 
    {
        super(null);
        this.featureOfInterest = featureOfInterest;
        this.observedProperty  = observedProperty;
        this.procedure         = procedure;
        this.distribution      = distribution;
        this.quality           = quality;
    }

    /**
     * Construit un nom à partir des autres informations disponibles.
     */
    @Override
    protected String createName() {
        return observedProperty.getName() + '(' + featureOfInterest.getName() + ')';
    }

    /**
     * {@inheritDoc}
     */
    public SamplingFeature getFeatureOfInterest() {
        return featureOfInterest;
    }

    /**
     * {@inheritDoc}
     */
    public Phenomenon getObservedProperty() {
        return observedProperty;
    }

    /**
     * {@inheritDoc}
     */
    public Process getProcedure() {
        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    public Distribution getDistribution() {
        return distribution;
    }
    
    /**
     * {@inheritDoc}
     */
    public DataQuality getQuality() {
        return quality;
    }


    /**
     * Retourne un code représentant cette observation.
     */
    @Override
    public final int hashCode() {
        return featureOfInterest.hashCode() ^ observedProperty.hashCode();
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
            final ObservationEntry that = (ObservationEntry) object;
            return Utilities.equals(this.featureOfInterest,    that.featureOfInterest) &&
                   Utilities.equals(this.observedProperty,   that.observedProperty) &&
                   Utilities.equals(this.procedure,    that.procedure)  &&
                   Utilities.equals(this.quality,      that.quality)    && 
                   Utilities.equals(this.distribution, that.distribution);
        }
        return false;
    }
}
