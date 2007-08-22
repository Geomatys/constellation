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

// Sicade
import net.sicade.catalog.Element;


/**
 * Une observation effectuée à une {@linkplain Station station}. Une même station peut contenir plusieurs
 * observations, à la condition que chaque observation porte sur un {@linkplain Observable observable}
 * différent. Une observation n'est pas forcément une valeur numérique. Il peut s'agir d'une observation
 * qualitative ou d'un vecteur par exemple. Lorsqu'une observation est quantifiée par une valeur numérique
 * scalaire, on utilisera le terme {@linkplain Measurement mesure}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Observation extends Element {
    /**
     * Retourne la station à laquelle a été effectuée cette observation.
     */
    SamplingFeature getStation();

    /**
     * Retourne le phénomène observé.
     */
    Observable getObservable();
}
