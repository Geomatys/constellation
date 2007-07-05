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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.Set;
import java.util.Date;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Phenomenon;
import net.sicade.observation.Procedure;
import net.sicade.observation.Observable;
import net.sicade.observation.CatalogException;


/**
 * Représentation une série d'images. Chaque série d'images portent sur un
 * {@linkplain Phenomenon phénomène} (par exemple la température) observé à l'aide d'une certaine
 * {@linkplain Procedure procédure} (par exemple une synthèse des données de plusieurs satellites
 * NOAA). Chaque série d'images étant la combinaison d'un phénomène avec une procédure, elles
 * forment donc des {@linkplain Observable observables}.
 * <p>
 * Dans le contexte particulier des séries d'images, le <cite>phénomène</cite> est appelé
 * {@linkplain Thematic thématique}.
 * <p>
 * Des opérations supplémentaires peuvent être appliquées sur une série d'image. Par exemple une
 * série peut représenter des images de températures, et un calcul statistique peut travailler
 * sur les gradients de ces images de température. Aux yeux d'entités de plus haut niveau (tels
 * les {@linkplain Descriptor descripteurs du paysage océanique}), une série d'images peut donc
 * être considérée comme un phénomène à combiner avec une autre procédure, en l'occurence une
 * {@linkplain Operation opération}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Series extends Observable, Phenomenon {
    /**
     * Retourne la thématique de cette série d'images. Des exemples de thématiques sont
     * la <cite>température</cite>, l'<cite>anomalie de la hauteur de l'eau</cite>,
     * la <cite>concentration en chlorophylle-a</cite>, etc.
     */
    Thematic getPhenomenon();

    /**
     * Retourne la procédure utilisée pour collecter les images. Par exemple il peut d'agir d'une
     * synthèse des données captées par plusieurs satellites NOAA.
     */
    Procedure getProcedure();

    /**
     * Une série de second recours qui peut être utilisée si aucune données n'est disponible
     * dans cette série à une certaine position spatio-temporelle. Retourne {@code null} s'il
     * n'y a pas de série de second recours.
     */
    Series getFallback();

    /**
     * Retourne les sous-ensembles de cette séries.
     */
    Set<SubSeries> getSubSeries();

    /**
     * Retourne l'intervalle de temps typique entre deux images consécutives de cette série.
     * Cette information n'est qu'à titre indicative. L'intervalle est exprimée en nombre de
     * jours. Cette méthode retourne {@link Double#NaN} si l'intervalle de temps est inconnu.
     */
    double getTimeInterval();

    /**
     * Retourne la plage de temps englobeant toutes les images de cette série.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Retourne les coordonnées géographiques englobeant toutes les images de cette série.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * Retourne une image appropriée pour la date spécifiée.
     *
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    CoverageReference getCoverageReference(Date time) throws CatalogException;

    /**
     * Retourne la liste des images disponibles dans la plage de coordonnées spatio-temporelles
     * de cette série. Les images ne seront pas immédiatement chargées; seules des références
     * vers ces images seront retournées.
     *
     * @return Liste d'images qui interceptent la plage de temps et la région géographique d'intérêt.
     * @throws CatalogException si le catalogue n'a pas pu être interrogé.
     */
    Set<CoverageReference> getCoverageReferences() throws CatalogException;

    /**
     * Retourne une vue des données de cette séries sous forme de fonction. Chaque valeur peut
     * être évaluée à une position (<var>x</var>,<var>y</var>,<var>t</var>), en faisant intervenir
     * des interpolations si nécessaire. Cette méthode retourne une fonction moins élaborée que
     * celle de {@link Descriptor#getCoverage} pour les raisons suivantes:
     * <p>
     * <ul>
     *   <li>Il n'y a ni {@linkplain Operation opération}, ni {@link LocationOffset décalage
     *       spatio-temporel} d'appliqués sur les données à évaluer.</li>
     *   <li>Les valeurs sont évaluées directement sur les images de cette série, jamais sur
     *       celles de la {@linkplain #getFallback série de second recours}.</li>
     *   <li>Des images entières peuvent être transiter sur le réseau, plutôt que seulement
     *       les valeurs à évaluer.</li>
     * </ul>
     *
     * @throws CatalogException si la fonction n'a pas pu être construite.
     */
    Coverage getCoverage() throws CatalogException;

    /**
     * Si cette série est le résultat d'un modèle numérique, retourne ce modèle.
     * Sinon, retourne {@code null}.
     *
     * @throws CatalogException si la base de données n'a pas pu être construite.
     */
    Model getModel() throws CatalogException;
}
