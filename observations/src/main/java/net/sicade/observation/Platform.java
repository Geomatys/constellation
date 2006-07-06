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

import java.util.Set;


/**
 * Plateforme sur laquelle sont effectu�es les {@linkplain Station stations}. Une plateforme peut �tre par
 * exemple un bateau que l'on suivra pendant une campagne d'�chantillonage. Des campagnes d'�chantillonages
 * diff�rentes seront typiquement consid�r�es comme des plateformes distinctes, m�me si le m�me bateau est
 * r�utilis� (on pourrait faire valoir qu'une nouvelle mission oc�anographique embarque souvent des instruments
 * diff�rents). Le choix d'utiliser des plateformes diff�rentes ou pas est laiss� au jugement du gestionnaire.
 * Le point cl� est qu'une plateforme contient une s�rie de stations que l'on peut relier entre elles par une
 * {@linkplain #getPath trajectoire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Platform extends LocatedElement {
    /**
     * Retourne toutes les stations visit�es par cette plateforme, en ordre chronologique.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Set<? extends Station> getStations() throws CatalogException;
}
