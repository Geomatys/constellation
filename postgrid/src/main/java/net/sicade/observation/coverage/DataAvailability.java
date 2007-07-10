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
package net.sicade.observation.coverage;

import java.util.Date;
import java.io.Serializable;

import org.geotools.util.RangeSet;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;


/**
 * Contient les plages de temps et de coordonnées couvertes par les images. Ces informations
 * sont fournies sous forme d'objets {@link RangeSet}, ce qui permet de connaître les trous
 * dans les données.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class DataAvailability implements Serializable {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -4275093602674070015L;

    /**
     * Les plages de longitudes, ou {@code null} si cette information n'a pas été demandée.
     */
    public final RangeSet x;

    /**
     * Les plages de latitudes, ou {@code null} si cette information n'a pas été demandée.
     */
    public final RangeSet y;

    /**
     * Les plages de temps, ou {@code null} si cette information n'a pas été demandée.
     */    
    public final RangeSet t;

    /** 
     * Construit des plages initialement vides pour les dimensions spécifiées.
     *
     * @param x {@code true} pour obtenir les plages de longitudes.
     * @param y {@code true} pour obtenir les plages de latitudes.
     * @param t {@code true} pour obtenir les plages de temps.
     */
    public DataAvailability(final boolean x, final boolean y, final boolean t, final boolean entries) {
        this.x = x ? new RangeSet(Longitude.class) : null;
        this.y = y ? new RangeSet(Latitude .class) : null;
        this.t = t ? new RangeSet(Date     .class) : null;
    }

    /**
     * Retourne {@code true} s'il est plausible qu'une donnée soit disponible à la position
     * spatio-temporelle spécifiée. Une valeur {@code true} ne garantie pas qu'une donnée est
     * disponible. En revanche, une valeur {@code false} garantie qu'il n'y a pas de données
     * pour la position spécifiée.
     *
     * @param x La longitude, ou {@code null} pour ne pas prendre en compte la longitude.
     * @param y La latitude,  ou {@code null} pour ne pas prendre en compte la latitude.
     * @param t La date,      ou {@code null} pour ne pas prendre en compte la date.
     * @return {@code true} s'il est possible qu'une donnée soit disponible à la coordonnée spécifiée,
     *         ou {@code false} s'il est garantie qu'il n'y a pas de donnée à cette position.
     */
    public boolean contains(final Longitude x, final Latitude y, final Date t) {
        return (x==null || this.x==null || this.x.indexOfRange(x)>=0) &&
               (y==null || this.y==null || this.y.indexOfRange(y)>=0) &&
               (t==null || this.t==null || this.t.indexOfRange(t)>=0);
    }
}
