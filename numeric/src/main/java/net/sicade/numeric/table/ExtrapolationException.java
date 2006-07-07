/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 1997, Pêches et Océans Canada
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
package net.sicade.numeric.table;


/**
 * Exception lancée lorsqu'une extrapolation a été effectuée alors que ce n'était pas permis.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExtrapolationException extends Exception {
    /**
     * Pour compatibilité avec des enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7105249847450063936L;

    /**
     * Valeur <var>x</var> pour laquelle une interpolation avait été demandée.
     */
    public final double xi;

    /**
     * Construit une exception déclarant que le vecteur
     * des <var>x</var> ne contient pas suffisament de données.
     */
    public ExtrapolationException() {
        super("Le vecteur ne contient pas suffisament de données.");
        xi = Double.NaN;
    }

    /**
     * Construit une exception déclarant que le vecteur des <var>x</var> ne contient pas
     * suffisament de données ou que la donnée <var>xi</var> demandée n'est pas valide,
     * si celle-ci est un NaN.
     *
     * @param xi valeur de <var>x</var> pour laquelle on voulait interpoler un <var>y</var>.
     */
    public ExtrapolationException(final double xi) {
        super(Double.isNaN(xi) ? "Ne peux pas interpoler à x=NaN."
                               : "La donnée demandée est en dehors de la plage de valeurs du vecteur des X.");
        this.xi = xi;
    }
}
