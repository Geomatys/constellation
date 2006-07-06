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
package net.sicade.observation.coverage;

import net.sicade.observation.Element;


/**
 * Un sous-ensemble d'une {@linkplain Series s�rie} d'image. Une s�rie d'images peut �tre divis�e
 * en plusieurs sous-ensembles, o� chaque sous-ensemble partage des caract�ristiques communes. Par
 * exemple une s�rie d'images de temp�rature de surface de la mer (SST) en provenance du programme
 * <cite>Pathfinder</cite> de la Nasa peut �tre divis�e en deux sous-ensembles:
 * <p>
 * <ul>
 *   <li>Les donn�es historiques "d�finitives" (pour une version donn�e de la cha�ne de traitement),
 *       souvent vieille d'un moins deux ans � cause de d�lai n�cessaire � leur traitement.</li>
 *   <li>Les donn�es plus r�centes mais pas encore d�finitives, appel�e "int�rimaires".</li>
 * </ul>
 * <p>
 * Une autre raison de diviser en sous-s�ries peut �tre un changement de format ou de cha�ne de
 * traitement des donn�es � partir d'une certaine date.
 * <p>
 * Pour la plupart des utilisations, cette distinction n'est pas utile et l'on travaillera uniquement
 * sur des s�ries d'images. Toutefois pour certaines applications, il peut �tre n�cessaire de faire
 * la distinction entre ces sous-ensembles.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface SubSeries extends Element {
    /**
     * Retourne le format des images de cette sous-s�rie.
     */
    Format getFormat();
}
