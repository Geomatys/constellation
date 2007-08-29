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


/**
 * Une {@linkplain Observation observation} dont la valeur est un scalaire.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Measurement extends Observation {
    /**
     * Retourne la valeur numérique de la mesure.
     */
    float getValue();

    /**
     * Retourne l'erreur sur la valeur de la mesure, ou {@link Float#NaN} si inconnue.
     */
    float getError();

    /**
     * Retourne les unités de la mesure. Ca peut être par exemple des kilogrammes
     * ou des tonnes de poissons pêchés, ou plus simplement un comptage du nombre
     * d'individus. Dans ce dernier cas, l'unité retournée sera sans dimension.
     */
    Unit getUom();
}
