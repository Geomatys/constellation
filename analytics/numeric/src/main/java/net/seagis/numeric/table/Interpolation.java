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
 * Type d'interpolation à utiliser pour obtenir une valeur <var>y</var> à partir d'une position
 * <var>x</var>.
 *
 * @author Martin Desruisseaux
 */
public enum Interpolation {
    /**
     * Plus proche voisin.
     */
    NEAREST,

    /**
     * Interpolation linéaire.
     */
    LINEAR
}
