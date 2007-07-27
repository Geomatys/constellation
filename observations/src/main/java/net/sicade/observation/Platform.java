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

import java.util.Set;
import net.sicade.coverage.catalog.CatalogException;


/**
 * Plateforme sur laquelle sont effectuées les {@linkplain Station stations}. Une plateforme peut être par
 * exemple un bateau que l'on suivra pendant une campagne d'échantillonage. Des campagnes d'échantillonages
 * différentes seront typiquement considérées comme des plateformes distinctes, même si le même bateau est
 * réutilisé (on pourrait faire valoir qu'une nouvelle mission océanographique embarque souvent des instruments
 * différents). Le choix d'utiliser des plateformes différentes ou pas est laissé au jugement du gestionnaire.
 * Le point clé est qu'une plateforme contient une série de stations que l'on peut relier entre elles par une
 * {@linkplain #getPath trajectoire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Platform extends LocatedElement {
    /**
     * Retourne toutes les stations visitées par cette plateforme, en ordre chronologique.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    Set<? extends Station> getStations() throws CatalogException;
}
