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
import java.util.List;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;


/**
 * Interface de base des modèles linéaires ou non-linéaires.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Model extends Element {
    /**
     * Retourne la série dans laquelle seront stockées les valeurs de la variable dépendante <var>y</var>.
     * C'est la série des images qui seront produites à partir de ce modèle.
     */
    Series getTarget();

    /**
     * Retourne l'ensemble des descripteurs utilisés comme entrées au modèle. Cet ensemble est
     * ordonné; à chaque index correspond une valeur réelle qui sera donnée à la méthode
     * {@link #evaluate}.
     */
    List<Descriptor> getDescriptors();

    /**
     * {@linkplain Distribution#normalize Normalise} toutes les données spécifiées. Il est de la
     * responsabilité de l'utilisateur d'appeller cette méthode exactement une fois avant d'appeller
     * la méthode {@link #evaluate evaluate}. La normalisation est faite sur place.
     */
    void normalize(double[] values);

    /**
     * Calcule la valeur prédite à partir des valeurs données en entrés. Chaque valeur à un index
     * <var>i</var> doit correspondre au descripteur à ce même index <var>i</var> dans l'ensemble
     * retournée par {@link #getDescriptors}.
     */
    double evaluate(double[] values);

    /**
     * Retourne une couverture qui évaluera le modèle aux positions spatio-temporelles qui lui
     * seront données.
     *
     * @throws CatalogException si la couverture n'a pas pu être construite.
     */
    Coverage asCoverage() throws CatalogException;
}
