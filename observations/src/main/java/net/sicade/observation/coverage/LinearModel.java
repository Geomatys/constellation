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
import java.util.Locale;
import java.io.Writer;
import java.io.IOException;

// Sicade dependencies
import net.sicade.observation.Element;


/**
 * Un modèle linéaire.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LinearModel extends Model {
    /**
     * Retourne les termes d'un modèle linéaire calculant le paramètre. Un paramètre peut être le
     * résultat d'une combinaison de d'autres paramètres, par exemple sous la forme de l'équation
     * suivante:
     *
     * <p align="center">{@code PP} = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;{@code SST} +
     * <var>C</var><sub>2</sub>&times;{@code SLA} +
     * <var>C</var><sub>3</sub>&times;{@code SST}&times;{@code SLA} + ...</p>
     *
     * Chacun des termes à droite du signe = est décrit par un objet
     * {@link net.sicade.observation.coverage.LinearModel.Term}.
     * Ces descriptions incluent le coefficient <var>C</var><sub>n</sub>,
     * qui résulte généralement d'une régression linéaire multiple.
     *
     * @return La liste de tous les termes composant le modèle linéaire. Cette liste est immutable.
     */
    List<Term> getTerms();

    /**
     * Écrit le modèle linéaire vers le flot de sortie spécifié. Cette méthode utilisera une
     * ligne par terme du modèle linéaire, et tentera d'aligner les termes comme dans un tableau.
     *
     * @param  out Le flot de sortie dans lequel écrire.
     * @param  locale Les conventions locales à utiliser, où {@code null} pour les conventions
     *         par défaut.
     * @throws IOException si une erreur est survenue lors de l'écriture.
     */
    void print(final Writer out, final Locale locale) throws IOException;

    /**
     * Un terme dans un modèle linéaire. Un modèle linéaire peut s'écrire de la forme suivante:
     *
     * <p align="center"><var>y</var> = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub> +
     * <var>C</var><sub>2</sub>&times;<var>x</var><sub>2</sub> +
     * <var>C</var><sub>3</sub>&times;<var>x</var><sub>3</sub> + ...</p>
     *
     * Dans ce modèle, le terme <var>C</var><sub>0</sub> est représenté par un objet
     * {@code LinearModel.Term}, le terme <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub>
     * par un autre objet {@code LinearModel.Term}, et ainsi de suite.
     * <p>
     * Les variables indépendantes <var>x</var><sub>1</sub>, <var>x</var><sub>2</sub>,
     * <cite>etc.</cite> sont les {@linkplain Descriptor descripteurs du paysage océanique},
     * eux-mêmes dérivés d'une {@linkplain Series série d'images} représentant un paramètre
     * environnemental.
     * <p>
     * La variable dépendante <var>y</var> sera stockée dans un nouveau paramètre 
     * environnemental (par exemple un paramètre appelé "potentiel de pêche"). 
     * Elle pourra donc servir d'entrée à un autre modèle linéaire.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public interface Term extends Element {
        /**
         * Retourne le coefficient <var>C</var> de ce terme. Ce coefficient a 
         * généralement été obtenu par une régression linéaire multiple.
         */
        double getCoefficient();

        /**
         * Retourne les descripteurs du paysage océanique composant ce terme. Par exemple, le terme
         * {@code this} pourrait être <var>C</var>&times;{@code SST}&times;{@code SLA}, où <var>C</var>
         * est le {@linkplain #getCoefficient coefficient} déterminé par la régression linéaire, tandis
         * que {@code SST} et {@code SLA} sont les valeurs {@linkplain Distribution#normalize normalisées}
         * de température de surface et d'anomalie de la hauteur de l'eau respectivement. Pour cet exemple,
         * {@code getDescriptors()} retournerait dans une liste les deux descripteurs {@code SST} et
         * {@code SLA}.
         */
        List<Descriptor> getDescriptors();
    }
}
