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
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.List;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.util.InternationalString;
import org.opengis.geometry.DirectPosition;

// Sicade dependencies
import net.sicade.observation.CatalogException;


/**
 * Couverture de données dans un espace spatio-temporelle. L'aspect "dynamique" vient de l'axe
 * temporel. Sur le plan de l'implémentation, chaque appel à une méthode {@code evaluate} à une
 * date différente se traduira typiquement par le chargement d'une nouvelle image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DynamicCoverage extends Coverage {
    /**
     * Retourne un nom pour cette couverture.
     */
    InternationalString getName();

    /**
     * Retourne les coordonnées au centre du voxel le plus proche des coordonnées spécifiées.
     * Cette méthode recherche l'image la plus proche de la date spécifiée, puis recherche le
     * pixel qui contient la coordonnée géographique spécifiée. La date de milieu de l'image,
     * ainsi que les coordonnées géographiques au centre du pixel, sont retournées. Appeller
     * la méthode {@link #evaluate evaluate} avec les coordonnées retournées devrait permettre
     * d'obtenir une valeur non-interpollée.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation de la
     *         base de données.
     */
    DirectPosition snap(DirectPosition position) throws CatalogException;

    /**
     * Retourne les couvertures utilisées par les méthodes {@code evaluate} pour le temps <var>t</var>
     * spécifié. L'ensemble retourné comprendra typiquement 0, 1 ou 2 éléments.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation de la
     *         base de données.
     */
    List<Coverage> coveragesAt(double t) throws CatalogException;
}
