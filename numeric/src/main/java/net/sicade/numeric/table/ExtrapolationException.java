/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 1997, P�ches et Oc�ans Canada
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
package net.sicade.numeric.table;


/**
 * Exception lanc�e lorsqu'une extrapolation a �t� effectu�e alors que ce n'�tait pas permis.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExtrapolationException extends Exception {
    /**
     * Pour compatibilit� avec des enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -7105249847450063936L;

    /**
     * Valeur <var>x</var> pour laquelle une interpolation avait �t� demand�e.
     */
    public final double xi;

    /**
     * Construit une exception d�clarant que le vecteur
     * des <var>x</var> ne contient pas suffisament de donn�es.
     */
    public ExtrapolationException() {
        super("Le vecteur ne contient pas suffisament de donn�es.");
        xi = Double.NaN;
    }

    /**
     * Construit une exception d�clarant que le vecteur des <var>x</var> ne contient pas
     * suffisament de donn�es ou que la donn�e <var>xi</var> demand�e n'est pas valide,
     * si celle-ci est un NaN.
     *
     * @param xi valeur de <var>x</var> pour laquelle on voulait interpoler un <var>y</var>.
     */
    public ExtrapolationException(final double xi) {
        super(Double.isNaN(xi) ? "Ne peux pas interpoler � x=NaN."
                               : "La donn�e demand�e est en dehors de la plage de valeurs du vecteur des X.");
        this.xi = xi;
    }
}
