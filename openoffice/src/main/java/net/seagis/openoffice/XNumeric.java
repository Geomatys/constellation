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
package net.seagis.openoffice;

// OpenOffice dependencies
import com.sun.star.uno.XInterface;
import com.sun.star.beans.XPropertySet;


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
