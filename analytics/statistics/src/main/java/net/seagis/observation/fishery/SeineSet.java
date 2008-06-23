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
package net.seagis.observation.fishery;

// OpenGis dependencies
import org.opengis.observation.sampling.SamplingFeature;


/**
 * Représentation d'un coup de senne.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface SeineSet extends SamplingFeature {
    /**
     * Indique si le bancs capturé était associé à un objet flottant.
     */
    Association getAssociation();

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
