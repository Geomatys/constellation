/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
 * Valeur et coordonn�es d'un pixel.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Pixel implements Comparable<Pixel> {
    /**
     * Coordonn�es du pixel.
     */
    int x,y;

    /**
     * Valeur du pixel.
     */
    double value;

    /**
     * Construit un nouveau pixel initialis� � 0.
     */
    Pixel() {
    }

    /**
     * Compare ce pixel avec le pixel sp�cifi�.
     */
    public int compareTo(final Pixel other) {
        return Double.compare(value, other.value);
    }

    /**
     * Retourne une repr�sentation de ce pixel sous forme de cha�ne de caract�re.
     * Utilis� qu'� des fins de d�boguages.
     */
    @Override
    public String toString() {
        return "Pixel(" + x + ", " + y + ") = " + value;
    }
}
