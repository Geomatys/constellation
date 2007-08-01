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
package net.sicade.coverage.catalog;

import java.util.Set;
import java.util.Date;
import java.util.SortedSet;
import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;
import net.sicade.util.DateRange;
import net.sicade.catalog.Element;
import net.sicade.catalog.CatalogException;


/**
 * Représentation une couche d'images. Chaque couche d'images portent sur un
 * {@linkplain Phenomenon phénomène} (par exemple la température) observé à l'aide d'une certaine
 * {@linkplain Procedure procédure} (par exemple une synthèse des données de plusieurs satellites
 * NOAA). Chaque couche d'images étant la combinaison d'un phénomène avec une procédure, elles
 * forment donc des {@linkplain Observable observables}.
 * <p>
 * Dans le contexte particulier des couches d'images, le <cite>phénomène</cite> est appelé
 * {@linkplain Thematic thématique}.
 * <p>
 * Des opérations supplémentaires peuvent être appliquées sur une couche d'image. Par exemple une
 * couche peut représenter des images de températures, et un calcul statistique peut travailler
 * sur les gradients de ces images de température. Aux yeux d'entités de plus haut niveau (tels
 * les {@linkplain Descriptor descripteurs du paysage océanique}), une couche d'images peut donc
 * être considérée comme un phénomène à combiner avec une autre procédure, en l'occurence une
 * {@linkplain Operation opération}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Layer extends Element {
    /**
     * Retourne la thématique de cette couche d'images. Des exemples de thématiques sont
     * la <cite>température</cite>, l'<cite>anomalie de la hauteur de l'eau</cite>,
     * la <cite>concentration en chlorophylle-a</cite>, etc.
     */
    Thematic getThematic();

    /**
     * Une couche de second recours qui peut être utilisée si aucune données n'est disponible
     * dans cette couche à une certaine position spatio-temporelle. Retourne {@code null} s'il
     * n'y a pas de couche de second recours.
     */
    Layer getFallback();

    /**
     * Returns all series for this layer.
     */
    Set<Series> getSeries();

    /**
     * Retourne l'intervalle de temps typique entre deux images consécutives de cette couche.
     * Cette information n'est qu'à titre indicative. L'intervalle est exprimée en nombre de
     * jours. Cette méthode retourne {@link Double#NaN} si l'intervalle de temps est inconnu.
     */
    double getTimeInterval();

    /**
     * Returns the set of dates when a coverage is available.
     *
     * @return The set of dates.
     * @throws CatalogException if the set can not be obtained.
     */
    SortedSet<Date> getAvailableTimes() throws CatalogException;

    /**
     * Returns the set of altitudes where a coverage is available. If different images
     * have different set of altitudes, then this method returns only the altitudes
     * found in every images.
     *
     * @return The set of altitudes. May be empty, but will never be null.
     * @throws CatalogException if the set can not be obtained.
     */
    SortedSet<Number> getAvailableElevations() throws CatalogException;

    /**
     * Retourne la plage de temps englobeant toutes les images de cette couche.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Retourne les coordonnées géographiques englobeant toutes les images de cette couche.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * Retourne une image appropriée pour la date spécifiée.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    CoverageReference getCoverageReference(Date time, Number elevation) throws CatalogException;

    /**
     * Retourne la liste des images disponibles dans la plage de coordonnées spatio-temporelles
     * de cette couche. Les images ne seront pas immédiatement chargées; seules des références
     * vers ces images seront retournées.
     *
     * @return Liste d'images qui interceptent la plage de temps et la région géographique d'intérêt.
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    Set<CoverageReference> getCoverageReferences() throws CatalogException;

    /**
     * Retourne une vue des données de cette couches sous forme de fonction. Chaque valeur peut
     * être évaluée à une position (<var>x</var>,<var>y</var>,<var>t</var>), en faisant intervenir
     * des interpolations si nécessaire. Cette méthode retourne une fonction moins élaborée que
     * celle de {@link Descriptor#getCoverage} pour les raisons suivantes:
     * <p>
     * <ul>
     *   <li>Il n'y a ni {@linkplain Operation opération}, ni {@link RegionOfInterest décalage
     *       spatio-temporel} d'appliqués sur les données à évaluer.</li>
     *   <li>Les valeurs sont évaluées directement sur les images de cette couche, jamais sur
     *       celles de la {@linkplain #getFallback couche de second recours}.</li>
     *   <li>Des images entières peuvent être transiter sur le réseau, plutôt que seulement
     *       les valeurs à évaluer.</li>
     * </ul>
     *
     * @throws CatalogException si la fonction n'a pas pu être construite.
     */
    Coverage getCoverage() throws CatalogException;

    /**
     * Si cette couche est le résultat d'un modèle numérique, retourne ce modèle.
     * Sinon, retourne {@code null}.
     *
     * @throws CatalogException si la base de données n'a pas pu être construite.
     */
    Model getModel() throws CatalogException;
}
