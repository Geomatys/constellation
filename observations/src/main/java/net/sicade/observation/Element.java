/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation;

import java.util.logging.Logger;


/**
 * Interface de base des �l�ments ayant un rapport avec les observations. Il s'agit en quelque sorte
 * de l'�quivalent de la classe {@link Object}; une interface de base pouvant repr�senter � peu pr�s
 * n'importe quoi. Les interfaces d�riv�es utiles comprennent les {@linkplain Observation observations}
 * et les {@linkplain net.sicade.observation.coverage.Series s�ries d'images} par exemple.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Element {
    /**
     * Le journal dans lequel enregistrer les �v�nements qui ont rapport avec les donn�es d'observations.
     * Ce journal peut archiver des �v�nements relatifs � certaines {@linkplain LoggingLevel#SELECT
     * consultations} de la base de donn�es, et surtout aux {@linkplain LoggingLevel#UPDATE mises � jour}.
     */
    Logger LOGGER = Logger.getLogger("net.sicade.observation");

    /**
     * Retourne le nom de cet �l�ment. Ce nom peut �tre d�riv� � partir d'une propri�t� arbitraire
     * de cet �l�ment. Par exemple dans le cas d'une image, il s'agira le plus souvent du nom du
     * fichier (sans son chemin). Le nom retourn� par cette m�thode devrait �tre suffisament parlant
     * pour �tre ins�r� dans une interface utilisateur (par exemple une liste d�roulante).
     */
    String getName();

    /**
     * Retourne des remarques s'appliquant � cette entr�e, ou {@code null} s'il n'y en a pas.
     * Ces remarques peuvent �tre par exemple une courte explication � faire appara�tre dans une
     * interface utilisateur comme "<cite>tooltip text</cite>".
     */
    String getRemarks();
}
