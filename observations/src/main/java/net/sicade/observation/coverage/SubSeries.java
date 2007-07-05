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
package net.sicade.observation.coverage;

import net.sicade.observation.Element;


/**
 * Un sous-ensemble d'une {@linkplain Series série} d'image. Une série d'images peut être divisée
 * en plusieurs sous-ensembles, où chaque sous-ensemble partage des caractéristiques communes. Par
 * exemple une série d'images de température de surface de la mer (SST) en provenance du programme
 * <cite>Pathfinder</cite> de la Nasa peut être divisée en deux sous-ensembles:
 * <p>
 * <ul>
 *   <li>Les données historiques "définitives" (pour une version donnée de la chaîne de traitement),
 *       souvent vieille d'un moins deux ans à cause de délai nécessaire à leur traitement.</li>
 *   <li>Les données plus récentes mais pas encore définitives, appelée "intérimaires".</li>
 * </ul>
 * <p>
 * Une autre raison de diviser en sous-séries peut être un changement de format ou de chaîne de
 * traitement des données à partir d'une certaine date.
 * <p>
 * Pour la plupart des utilisations, cette distinction n'est pas utile et l'on travaillera uniquement
 * sur des séries d'images. Toutefois pour certaines applications, il peut être nécessaire de faire
 * la distinction entre ces sous-ensembles.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface SubSeries extends Element {
    /**
     * Retourne le format des images de cette sous-série.
     */
    Format getFormat();
}
