/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation.fishery;

// Sicade dependencies
import net.sicade.observation.Station;


/**
 * Repr�sentation d'une ligne de palangre.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface LongLine extends Station {
    /**
     * Retourne le nombre d'hame�ons.
     */
    int getHookCount();

    /**
     * Retourne la longueur de la palangre.
     */
    float getLineLength();

    /**
     * Retourne la distance au bateau le plus proche du m�me jour,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestNeighbor();

    /**
     * Retourne la distance � la c�te la plus proche,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestCoast();
}
