/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.image.operation;


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
