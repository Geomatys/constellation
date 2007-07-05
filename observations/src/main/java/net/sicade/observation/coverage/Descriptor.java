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

import java.util.Comparator;
import net.sicade.observation.Observable;
import net.sicade.observation.Distribution;     // Pour javadoc
import net.sicade.observation.CatalogException;


/**
 * Un descripteur du paysage océanique. Un descripteur est une variable explicative
 * donnée en entré aux modèles linéaires. Un descripteur du paysage océanique comprend:
 * <p>
 * <ul>
 *   <li>une {@linkplain Series série d'images} contenant les données du paramètre environnemental;</li>
 *   <li>une {@linkplain Operation opération} à appliquer (par exemple un opérateur de Sobel pour
 *       calculer les gradients);</li>
 *   <li>une {@linkplain LocationOffset position relative} à laquelle évaluer le résultat de
 *       l'opération sur le paramètre environnemental;</li>
 *   <li>une {@linkplain Distribution distribution théorique}, que l'on essaiera de ramener à la
 *       distribution normale par un changement de variable.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Descriptor extends Observable {
    /**
     * Un comparateur pour classer des descripteurs en ordre croissant de
     * {@linkplain LocationOffset#getDayOffset décalage temporel}. Si deux
     * descripteurs ont le même décalage temporels, alors d'autres critères
     * telles que le nom de la série peuvent être utilisés.
     */
    Comparator<Descriptor> TIME_ORDER = new Comparator<Descriptor>() {
        public int compare(final Descriptor d1, final Descriptor d2) {
            final double dt1 = d1.getLocationOffset().getDayOffset();
            final double dt2 = d2.getLocationOffset().getDayOffset();
            if (dt1 < dt2) return -1;
            if (dt1 > dt2) return +1;
            int c = d1.getPhenomenon().getName().compareTo(d2.getPhenomenon().getName());
            if (c != 0) {
                return c;
            }
            return d1.getName().compareTo(d2.getName());
        }
    };

    /**
     * Retourne la série d'images d'où proviennent les données du paramètre environnemental étudié.
     * Il peut s'agir par exemple d'une série d'images de température.
     */
    Series getPhenomenon();

    /**
     * Retourne l'opération appliquée sur les images de la série. Il peut s'agir par exemple
     * d'un opérateur de gradient. Si aucune opération n'est appliquée, alors cette méthode
     * retourne une opération identité.
     */
    Operation getProcedure();

    /**
     * Retourne la position relative à laquelle évaluer les images de la série.
     * Cette position est relative aux positions des observations.
     */
    LocationOffset getLocationOffset();

    /**
     * Retourne le numéro de la bande dans laquelle extraire les valeurs des images.
     */
    short getBand();

    /**
     * Retourne {@code true} si ce descripteur est le <cite>descripteur identité</cite>.
     * Le "descripteur identité" est un descripteur artificiel représentant une image
     * dont tous les pixels auraient la valeur 1. Il est utilisé dans des expressions de
     * la forme <code>y = C0 + C1*x + C2*x² + ...</code>, ou {@code C0} peut s'écrire
     * <code>C0&times;identité</code>.
     */
    boolean isIdentity();

    /**
     * Retourne une vue des données de ce descripteur sous forme de fonction. Chaque valeur peut
     * être évaluée à une position (<var>x</var>,<var>y</var>,<var>t</var>), en faisant intervenir
     * des interpolations si nécessaire. Cette méthode retourne une fonction plus élaborée que celle
     * de {@link Series#getCoverage} pour les raisons suivantes:
     * <p>
     * <ul>
     *   <li>Une {@linkplain #getProcedure opération} peut-être appliquée sur les images (par
     *       exemple calcul de un gradient)</li>
     *   <li>Un {@linkplain #getLocationOffset décalage spatio-temporel} peut être appliquée
     *       sur la position à laquelle évaluer les données.</li>
     *   <li>En cas de donnée manquante, la {@linkplain Series#getFallback série de second
     *       recours} est testée.</li>
     *   <li>Les données peuvent être évaluées sur un serveur distant sans jamais transmettre
     *       d'images complètes via le réseau.</li>
     * </ul>
     *
     * @throws CatalogException si la fonction n'a pas pu être construite.
     */
    DynamicCoverage getCoverage() throws CatalogException;
}
