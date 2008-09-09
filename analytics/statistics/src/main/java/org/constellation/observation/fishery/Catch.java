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
package net.seagis.observation.fishery;

// openGis dependencies
import org.opengis.observation.Measurement;


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
