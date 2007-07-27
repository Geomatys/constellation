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

import net.sicade.coverage.catalog.Distribution;
import net.sicade.catalog.Element;


/**
 * Représentation d'un {@linkplain Phenomenon phénomène} observé à l'aide d'une certaine
 * {@linkplain Procedure procedure}. Un observable peut être un paramètre physique tel que
 * la température de surface de la mer, un paramètre halieutique tel que la quantité d'espadons
 * pêchés, ou d'autre observables propres à un domaine d'étude.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Observable extends Element {
    /**
     * Retourne un numéro unique identifiant cet observable. Ce numéro est complémentaire (et dans
     * une certaine mesure redondant) avec {@linkplain #getName le symbole} de l'observable. Il
     * existe parce que les observables, ainsi que les {@linkplain Station stations}, sont
     * référencés dans des millions de lignes dans la table des observations.
     */
    int getNumericIdentifier();

    /**
     * Retourne le phénomène observé. Si cet observable représente un paramètre physique tel
     * que la température de surface de la mer, alors le phénomène retourné sera typiquement un
     * {@linkplain net.sicade.observation.coverage.Thematic thème}. Si cet observable représente
     * une quantité liée à une certaine espèce de poisson (par exemple la quantité pêchée, mais ça
     * pourrait être d'autres quantités obtenues selon d'autres {@linkplain Procedure procédures}),
     * alors le phénomène retourné sera typiquement une
     * {@linkplain net.sicade.observation.fishery.Species espèce}.
     */
    Phenomenon getPhenomenon();

    /**
     * Retourne la procedure utilisée. Si cet observable représente un paramètre physique tel que
     * la température de surface de la mer, alors la procédure retournée sera typiquement un
     * {@linkplain net.sicade.observation.coverage.Operation opérateur d'images}. Si cet observable
     * représente une quantité de poisson pêchée, alors la procédure retournée sera typiquement un
     * {@linkplain net.sicade.observation.fishery.FisheryType type de pêche}.
     */
    Procedure getProcedure();

    /**
     * Retourne la distribution statistique approximative des valeurs attendues. Cette distribution
     * n'est pas nécessairement déterminée à partir des données, mais peut être un <cite>a-priori</cite>.
     * Cette information est utilisée principalement par les
     * {@linkplain net.sicade.observation.coverage.LinearModel modèles linéaires} qui ont besoin d'une
     * <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">distribution normale</A>.
     * Cette méthode retourne {@code null} si la distribution est inconnue ou n'est pas pertinente
     * pour cet observable.
     */
    Distribution getDistribution();
}
