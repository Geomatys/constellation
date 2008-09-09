/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.observation.fishery;

// OpenGis dependencies
import org.opengis.observation.sampling.SamplingFeature;


/**
 * Représentation d'une ligne de palangre.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface LongLine extends SamplingFeature {
    /**
     * Retourne le nombre d'hameçons.
     */
    int getHookCount();

    /**
     * Retourne la longueur de la palangre.
     */
    float getLineLength();

    /**
     * Retourne la distance au bateau le plus proche du même jour,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestNeighbor();

    /**
     * Retourne la distance à la côte la plus proche,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestCoast();
}
