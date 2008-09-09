/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package net.seagis.numeric.table;


/**
 * Ordre des données dans un {@linkplain OrderedVector vecteur}. Les données peuvent être croissantes
 * ou décroissantes, strictement ou pas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum DataOrder {
    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_ASCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont croissantes.
     * Le vecteur contient quelques données consécutives de même valeur.
     */
    ASCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} ont la même valeur.
     */
    FLAT,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont décroissantes.
     * Le vecteur contient quelques données consécutives de même valeur.
     */
    DESCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_DESCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} ne sont pas ordonnées.
     */
    UNORDERED
}
