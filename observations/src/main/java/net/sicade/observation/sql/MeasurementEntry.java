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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.sql;

import javax.units.Unit;
import net.sicade.observation.Station;
import net.sicade.observation.Observable;
import net.sicade.observation.Measurement;


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
     * La valeur mesurée.
     */
    private final float value;

    /**
     * Estimation de l'erreur sur la valeur mesurée, ou {@link Double#NaN NaN} si l'erreur
     * est inconnue ou ne s'applique pas.
     */
    private final float error;
    
    /** 
     * Crée une nouvelle mesure.
     *
     * @param station     La station d'observation (par exemple une position de pêche).
     * @param observable  Ce que l'on observe (température, quantité pêchée, <cite>etc.</cite>).
     * @param value       La valeur mesurée.
     * @param error       Estimation de l'erreur sur la valeur mesurée, ou {@link Float#NaN NaN}
     *                    si l'erreur est inconnue ou ne s'applique pas.
     */
    protected MeasurementEntry(final Station    station, 
                               final Observable observable, 
                               final float      value, 
                               final float      error) 
    {
        super(station, observable);
        this.value = value;
        this.error = error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    StringBuilder createName() {
        final StringBuilder name = super.createName();
        name.append(" = ");
        name.append(value);
        if (!Float.isNaN(error)) {
            name.append(" \u00B1 ");
            name.append(error);
        }
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @todo Implémenter le retour des unités.
     */
    public Unit getUnit() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public float getError() {
        return error;
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final MeasurementEntry that = (MeasurementEntry) object;
            return Float.floatToIntBits(this.value) == Float.floatToIntBits(that.value) &&
                   Float.floatToIntBits(this.error) == Float.floatToIntBits(that.error);
        }
        return false;
    }
}
