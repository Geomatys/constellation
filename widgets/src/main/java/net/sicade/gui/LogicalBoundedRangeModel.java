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
package net.sicade.gui;

import javax.swing.BoundedRangeModel;


/**
 * Extension de l'interface {@link BoundedRangeModel} de <cite>Swing</cite>. Cette interface tente
 * de contourner une limitation embêtante de <cite>Swing</cite>, à savoir que son modèle ne travaille
 * qu'avec des entiers. Cette classe {@code LogicalBoundedRangeModel} offre une méthode
 * {@link #setLogicalRange} qui permet de spécifier les minimums et maximums de la plage de valeurs
 * en utilisant des nombres réels plutôt que seulement des entiers. Cette plage n'affecte aucunement
 * les minimums et maximums retournées comme valeurs entières par les méthodes {@link #getMinimum}
 * et {@link #getMaximum}. Elle n'affecte que la façon dont se font les conversions entre les
 * {@code int} et les autres types de nombres. Ces conversions se font par les méthodes
 * {@link #toInteger} et {@link #toLogical}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LogicalBoundedRangeModel extends BoundedRangeModel {
    /**
     * Spécifie la plage des valeurs non-entières. Cette méthode n'affecte <u>pas</u> la plage
     * des valeur entières du modèle {@link BoundedRangeModel} de base. Toutefois, elle affecte
     * la façon dont les conversions seront effectuées par les méthodes {@link #toLogical} et
     * {@link #toInteger}.
     *
     * @param minimum Valeur minimale. La valeur {@code null} indique qu'il faut prendre
     *        une valeur par défaut. La valeur minimale peut être obtenue par un appel à
     *        {@code toLogical(getMinimum())}.
     *
     * @param maximum Valeur maximale. La valeur {@code null} indique qu'il faut prendre
     *        une valeur par défaut. La valeur maximale peut être obtenue par un appel à
     *        {@code toLogical(getMaximum())}.
     */
    void setLogicalRange(double minimum, double maximum);

    /**
     * Convertit un entier du modèle vers un nombre plus général. La conversion dépendra
     * de la plage spécifiée par {@link #setLogicalRange}. Cette méthode est l'inverse de
     * {@link #toInteger}.
     */
    double toLogical(int integer);

    /**
     * Convertit une valeur réelle en entier utilisable par le modèle. La conversion dépendra
     * de la plage spécifiée par {@link #setLogicalRange}. Cette méthode est l'inverse de
     * {@link #toLogical}.
     */
    int toInteger(double logical);
}
