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
package net.sicade.observation.fishery;

// Sicade dependencies
import net.sicade.observation.Measurement;


/**
 * Une capture faites lors d'un {@linkplain SeineSet coups de senne} ou par une
 * {@linkplain LongLine palangre}. Un coup de sennes ou une palangre comprendront
 * généralement plusieurs captures, une pour chaque {@linkplain Category catégorie}
 * de poisson pêché.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Catch extends Measurement {
    /**
     * Retourne la catégorie de poissons capturés.
     */
    Category getObservable();

    /**
     * Retourne le comptage des prises.
     */
    int getCount();
}
