/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui;

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
