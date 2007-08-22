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
import net.sicade.observation.SamplingFeature;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import org.geotools.resources.Utilities;


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
    private final SamplingFeature station;

    /**
     * Ce que l'on observe
     */
    private final Observable observable;

    /**
     * Construit une observation.
     * 
     * @param station     La station d'observation (par exemple une position de pêche).
     * @param observable  Ce que l'on observe (température, quantité pêchée, <cite>etc.</cite>).
     */
    protected ObservationEntry(final SamplingFeature    station, 
                               final Observable observable) 
    {
        super(null);
        this.station    = station;
        this.observable = observable;
    }

    /**
     * Construit un nom à partir des autres informations disponibles.
     */
    @Override
    protected String createName() {
        return observable.getName() + '(' + station.getName() + ')';
    }

    /**
     * {@inheritDoc}
     */
    public SamplingFeature getStation() {
        return station;
    }

    /**
     * {@inheritDoc}
     */
    public Observable getObservable() {
        return observable;
    }

    /**
     * Retourne un code représentant cette observation.
     */
    @Override
    public final int hashCode() {
        return station.hashCode() ^ observable.hashCode();
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
            return Utilities.equals(this.observable, that.observable) && 
                   Utilities.equals(this.station,    that.station);
        }
        return false;
    }
}
