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

import net.sicade.catalog.Element;


/**
 * Représentation d'une procédure utilisée pour observer un {@linkplain Phenomenon phénomène}.
 * Cette interface est étendue dans des paquets spécialisés. Par exemple pour les images, les
 * procédures utilisées sont des {@linkplain net.sicade.observation.coverage.Operation opérations}.
 * Pour les données de pêches, les procédures utilisées sont des
 * {@linkplain net.sicade.observation.fishery.FisheryType types de pêche}.
 * <p>
 * La combinaison d'un {@linkplain Phenomenon phénomène} avec une procedure donne un
 * {@linkplain Observable observable}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Procedure extends Element {
}
