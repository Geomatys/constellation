/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package net.sicade.observation;

import java.util.Collection;
import net.sicade.catalog.CatalogException;
import org.opengis.metadata.citation.Citation;

/**
 * Représentation d'une station à laquelle ont été effectuées des {@linkplain Observation observations}.
 * Une station peut ne pas être localisée en un point précis, mais plutôt dans une certaine région. La
 * méthode {@link #getCoordinate} retourne les coordonnées d'un point que l'on suppose représentatif
 * (par exemple au milieu d'une zone de pêche à la senne), tandis que {@link #getPath} retourne
 * une forme qui représente la forme de la station. Cette forme n'est pas obligatoirement le contour
 * de la station. Par exemple il peut s'agir d'une ligne représentant la ligne d'une pêche à la palangre.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface SamplingFeature extends LocatedElement{
    /**
     * Retourne un numéro unique identifiant cette station. Ce numéro est complémentaire (et dans
     * une certaine mesure redondant) avec {@linkplain #getName le nom} de la station. Il existe
     * parce que les stations, ainsi que les {@linkplain Observable observables}, sont référencées
     * dans des millions de lignes dans la table des {@linkplain Observation observations}.
     */
    int getNumericIdentifier();

    /**
     * Retourne une indication sur la provenance de la donnée. Peut être {@code null} si cette
     * information n'est pas disponible.
     */
    Citation getProvider();

    /**
     * Retourne la plateforme transportant la station. Il s'agit par exemple d'un identifiant
     * d'un bateau ou un numéro de croisière. Peut être {@code null} si cette information n'est
     * pas disponible.
     
      SamplingFeatureCollection getPlatform();
     */
   
    /**
     * Retourne l'observation correspondant à l'observable spécifié. Si aucune observation n'a
     * été effectuée pour cet observable, retourne {@code null}.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     
    Observation getObservation(Observable observable) throws CatalogException;
     */
    
    /**
     * Retourne l'ensemble des observations qui ont été effectuées à cette station. Une même station
     * peut contenir plusieurs {@linkplain Observation observations}, à la condition que chaque
     * observation porte sur un {@linkplain Observable observable} différent.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    Collection<? extends Observation> getRelatedObservations() throws CatalogException;
}
