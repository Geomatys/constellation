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
package org.constellation.image.operation;


/**
 * Valeur et coordonnées d'un pixel.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Pixel implements Comparable<Pixel> {
    /**
     * Coordonnées du pixel.
     */
    int x,y;

    /**
     * Valeur du pixel.
     */
    double value;

    /**
     * Construit un nouveau pixel initialisé à 0.
     */
    Pixel() {
    }

    /**
     * Compare ce pixel avec le pixel spécifié.
     */
    public int compareTo(final Pixel other) {
        return Double.compare(value, other.value);
    }

    /**
     * Retourne une représentation de ce pixel sous forme de chaîne de caractère.
     * Utilisé qu'à des fins de déboguages.
     */
    @Override
    public String toString() {
        return "Pixel(" + x + ", " + y + ") = " + value;
    }
}
