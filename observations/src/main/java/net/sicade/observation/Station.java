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

import java.util.Collection;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;


/**
 * Repr�sentation d'une station � laquelle ont �t� effectu�es des {@linkplain Observation observations}.
 * Une station peut ne pas �tre localis�e en un point pr�cis, mais plut�t dans une certaine r�gion. La
 * m�thode {@link #getCoordinate} retourne les coordonn�es d'un point que l'on suppose repr�sentatif
 * (par exemple au milieu d'une zone de p�che � la senne), tandis que {@link #getPath} retourne
 * une forme qui repr�sente la forme de la station. Cette forme n'est pas obligatoirement le contour
 * de la station. Par exemple il peut s'agir d'une ligne repr�sentant la ligne d'une p�che � la palangre.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Station extends LocatedElement {
    /**
     * Retourne un num�ro unique identifiant cette station. Ce num�ro est compl�mentaire (et dans
     * une certaine mesure redondant) avec {@linkplain #getName le nom} de la station. Il existe
     * parce que les stations, ainsi que les {@linkplain Observable observables}, sont r�f�renc�es
     * dans des millions de lignes dans la table des {@linkplain Observation observations}.
     */
    int getNumericIdentifier();

    /**
     * Retourne une indication sur la provenance de la donn�e. Peut �tre {@code null} si cette
     * information n'est pas disponible.
     */
    Citation getProvider();

    /**
     * Retourne la plateforme transportant la station. Il s'agit par exemple d'un identifiant
     * d'un bateau ou un num�ro de croisi�re. Peut �tre {@code null} si cette information n'est
     * pas disponible.
     */
    Platform getPlatform();

    /**
     * Retourne une indication de la qualit� de la donn�e. Peut �tre {@code null} si cette
     * information n'est pas disponible.
     */
    DataQuality getQuality();

    /**
     * Retourne l'observation correspondant � l'observable sp�cifi�. Si aucune observation n'a
     * �t� effectu�e pour cet observable, retourne {@code null}.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Observation getObservation(Observable observable) throws CatalogException;

    /**
     * Retourne l'ensemble des observations qui ont �t� effectu�es � cette station. Une m�me station
     * peut contenir plusieurs {@linkplain Observation observations}, � la condition que chaque
     * observation porte sur un {@linkplain Observable observable} diff�rent.
     *
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    Collection<? extends Observation> getObservations() throws CatalogException;
}
