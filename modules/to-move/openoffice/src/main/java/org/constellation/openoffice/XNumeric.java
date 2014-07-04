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
package org.constellation.openoffice;

// OpenOffice dependencies

import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.XInterface;


/**
 * Pont en Java de l'interface IDL pour le service {@code XCoverage3D} déclaré dans le paquet
 * {@code XNumeric.idl}. Cette interface existe principalement pour satisfaire les environnements
 * IDE. Le fichier JAR final devrait plutôt inclure le fichier {@code .class} généré par l'outil
 * {@code javamaker} du SDK d'OpenOffice.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public interface XNumeric extends XInterface {
    /**
     * Retourne les index autour de la valeur <var>x<sub>i</sub></var> spécifiée. Le tableau
     * {@code data} doit contenir au minimum une colonne, celle des <var>x</var>. Toutes les
     * colonnes suivantes (optionnelles) sont des <var>y</var>. Si une valeur {@link Double#NaN
     * NaN} est trouvée dans l'une de ces colonnes, les index retournés éviteront de pointer vers
     * la ligne correspondante.
     *
     * @param xOptions Propriétés fournies par OpenOffice.
     * @param data     Les données (<var>x</var>, <var>y1</var>, <var>y2</var>, <cite>etc.</cite>).
     * @param xi       La valeur <var>x<sub>i</sub></var> pour laquelle on veut les index.
     * @return Index des lignes.
     */
    double[][] getLocatedIndex(XPropertySet xOptions, double[][] data, double xi);

    /**
     * Interpole les valeurs de <var>y</var> pour les valeurs <var>x<sub>i</sub></var> spécifiées.
     * Le tableau {@code data} devrait contenir au minimum deux colonnes. La première colonne est
     * celle des <var>x</var>, et toutes les colonnes suivantes sont celles des <var>y</var>. La
     * sortie sera une matrice de même dimension que le tableau {@code xi}.
     *
     * @param xOptions Propriétés fournies par OpenOffice.
     * @param data     Les données (<var>x</var>, <var>y1</var>, <var>y2</var>, <cite>etc.</cite>).
     * @param xi       Les valeurs <var>x<sub>i</sub></var> auxquelles interpoller.
     * @param interpolation Le type d'interpolation: {@code "nearest"}, {@code "linear"}.
     * @param skipMissingY  {@code true} pour ignorer les valeurs manquantes dans les vecteurs des
     *                      <var>y</var>, ou {@code false} pour retourner {@link Double#NaN NaN} si
     *                      de telles valeurs sont rencontrées.
     * @return Les valeurs interpolées.
     */
    double[][] getInterpolated(XPropertySet xOptions, double[][] data, double[][] xi,
                               Object interpolation, Object skipMissingY);
}
