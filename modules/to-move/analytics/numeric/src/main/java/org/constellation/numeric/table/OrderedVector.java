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
package org.constellation.numeric.table;

// J2SE dependencies
import java.util.Arrays;   // For javadoc
import java.nio.DoubleBuffer;

// OpenGIS dependencies
import org.opengis.util.Cloneable;

// Static imports
import static java.lang.Math.abs;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.System.arraycopy;
import static org.apache.sis.util.ArraysExt.isSorted;


/**
 * Un vecteur de valeurs <var>x</var> dont toutes les données sont en ordre croissant ou décroissant.
 * Les valeurs {@link Double#NaN NaN} ne sont pas ordonnées et peuvent apparaître n'importe où dans
 * ce vecteur. Notez que cette dernière règle est différente de celle de {@link Arrays#sort(double[])},
 * qui place les valeurs {@link Double#NaN NaN} à la fin du vecteur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class OrderedVector implements Cloneable {
	/**
     * Index des valeurs inférieure ({@code lower}) et supérieure ({@code upper}) à la valeur
     * spécifiée lors du dernier appel de {@link #locate(double)}.
	 */
    protected int lower, upper;

    /**
     * Valeur pour laquelle l'utilisateur a demandé une interpolation. Cette valeur est mémorisée
     * lors des appels à une méthode {@code locate}. Si le dernier appel d'une telle méthode a
     * retourné {@code true} et que {@code value} n'est pas {@link Double#NaN NaN}, alors la
     * condition suivante doit être respectée:
     *
     * <blockquote><pre>
     * {@linkplain #get get}({@linkplain #lower}) &lt= value &lt= {@linkplain #get get}({@linkplain #upper})
     * </pre></blockquote>
     */
    protected double value = Double.NaN;

    /**
     * Construit une nouvelle instance d'un vecteur ordonné.
     */
    public OrderedVector() {
    }

    /**
     * Retourne l'ordre des données dans ce vecteur. Les valeurs {@link Double#NaN NaN}
     * ne sont pas prises en compte pour déterminer l'ordre.
     */
    public abstract DataOrder getDataOrder();

    /**
     * Retourne la longueur de ce vecteur.
     */
    public abstract int length();

    /**
     * Retourne la valeur à l'index spécifié. La valeur de cet index peut varier de 0 inclusivement
     * jusqu'à {@link #length} exclusivement.
     * <p>
     * Cette méthode peut être considérée comme l'inverse de {@link #locate} dans la mesure où
     * après un appel à <code>{@linkplain #locate locate}({@linkplain #get get}(index))</code>,
     * on aura {@link #upper} == {@link #lower} == {@code index}.
     *
     * @param  index La valeur de l'index.
     * @return La valeur du vecteur à l'index spécifié.
     * @throws IndexOutOfBoundsException si l'index est en dehors des limites permises.
     */
    public abstract double get(int index) throws IndexOutOfBoundsException;

    /**
     * Trouve les index {@link #lower} et {@link #upper} qui permettent d'encadrer la valeur
     * spécifiée. Après l'appel de cetet méthode, les conditions suivantes sont respectées:
     * <p>
     * <ul>
     *   <li><b>Si</b> une correspondance exacte est trouvée entre la valeur <var>x</var>
     *       demandée et une des valeurs de ce vecteur, <b>alors:</b>
     *     <ul>
     *       <li>{@link #lower} == {@link #upper}</li>
     *       <li><code>{@linkplain #get get}({@linkplain #lower})</code> == <var>x</var> ==
     *           <code>{@linkplain #get get}({@linkplain #upper})</code></li>
     *     </ul>
     *   </li>
     *
     *   <li><b>Sinon:</b>
     *     <ul>
     *       <li>{@link #lower} &lt; {@link #upper}</li>
     *       <li>Une des conditions suivantes:
     *         <ul>
     *           <li><code>{@linkplain #get get}({@linkplain #lower})</code> &lt; <var>x</var> &lt;
     *               <code>{@linkplain #get get}({@linkplain #upper})</code>
     *               si les données de ce vecteur sont en ordre croissant, ou</li>
     *           <li><code>{@linkplain #get get}({@linkplain #lower})</code> &gt; <var>x</var> &gt;
     *               <code>{@linkplain #get get}({@linkplain #upper})</code>
     *               si les données de ce vecteur sont en ordre décroissant.</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>
     * Mis à part les correspondances exactes, cette méthode produira dans la plupart des cas un
     * résultat tel que <code>{@linkplain #upper} == {@linkplain #lower}+1</code>. Si toutefois
     * ce vecteur contient des valeurs {@link Double#NaN NaN}, alors l'écart entre {@link #lower}
     * et {@link #upper} peut être plus grand, car cette méthode s'efforce de faire pointer les
     * index {@link #lower} et {@link #upper} vers des valeurs réelles.
     * <p>
     * <strong>Exemple:</strong>
     * Supposons que ce vecteur contient les données suivantes:
     *
     * <blockquote><pre>
     * Indices: [  0   1   2   3   4   5   6   7   8]
     * Valeurs: {  4   9  12 NaN NaN  34  56  76  89}
     * </pre></blockquote>
     *
     * Alors,
     * <p>
     * <ul>
     *   <li>{@code locate( 9)} donnera  {@code lower=1}  et  {@code upper=1}.</li>
     *   <li>{@code locate(60)} donnera  {@code lower=6}  et  {@code upper=7}.</li>
     *   <li>{@code locate(20)} donnera  {@code lower=2}  et  {@code upper=5}.</li>
     * </ul>
     * <p>
     * Cette méthode peut être considérée comme l'inverse de {@link #get}.
     *
     * @param  x valeur à rechercher dans ce vecteur.
     * @return {@code true} si la valeur spécifiée est comprise dans la plage de ce vecteur, ou
     *         {@code false} si elle se trouve en dehors ou si le vecteur n'a pas suffisamment de
     *         données autres que {@link Double#NaN NaN}.
     */
    public abstract boolean locate(double x);

    /**
     * Positionne les index {@link #lower} et {@link #upper} autour de l'index spécifié. Si ce
     * vecteur ne contient pas de valeurs {@link Double#NaN NaN}, alors {@link #lower} sera égal
     * à {@code index-1} et {@link #upper} sera égal à {@code index+1}. Si ce vecteur contient
     * des valeurs {@link Double#NaN NaN}, alors {@link #lower} peut être un peu plus bas et/ou
     * {@link #upper} un peu plus haut de façon à pointer sur des valeurs réelles.
     *
     * @param  index Index de la valeur autour de laquelle on veut se positionner.
     * @return {@code true} si les bornes {@link #lower} et {@link #upper} ont été définie,
     *         ou {@code false} si l'index spécifié tombe en dehors des limites du vecteur.
     */
    public abstract boolean locateAroundIndex(int index);

    /**
     * Copie dans le tableau spécifié en argument la valeur des champs {@link #lower} et {@link #upper}.
     * Ce tableau peut avoir diverses longueurs. Un cas typique est lorsque qu'il a une longueur de 2.
     * Alors le champs {@link #lower} sera simplement copié dans {@code index[0]} et {@link #upper}
     * dans {@code index[1]}. Si le tableau à une longueur de 1, alors seul {@link #lower} sera copié
     * dans {@code index[0]}. Si le tableau à une longueur de 0, rien ne sera fait.
     * <p>
     * Les cas le plus intéressants se produisent lorsque le tableau {@code index} à une longueur
     * de 3 et plus. Cette méthode copiera les valeurs de {@link #lower} et {@link #upper} au milieu
     * de ce tableau, puis complètera les autres cellules avec la suite des index qui pointent vers
     * des valeurs autres que {@link Double#NaN NaN}. Par exemple si ce vecteur contient:
     *
     * <blockquote><pre>
     * Indices: [  0   1   2   3   4   5   6   7]
     * Valeurs: {  5   8 NaN  12 NaN  19  21  34}
     * </pre></blockquote>
     *
     * Alors l'appel de la méthode <code>{@link #locate locate}(15)</code> donnera aux champs
     * {@link #lower} et {@link #upper} les valeurs 3 et 5 respectivement, de sorte que
     * <code>{@linkplain #get get}(3) &lt; 15 &lt; {@linkplain #get get}(5)</code>. Si vous souhaitez
     * effectuer une interpolation polynomiale d'ordre 4 autour de ces données, vous pouvez écrire:
     *
     * <blockquote><pre>
     * if (locate(x)) {
     *     int index[] = new int[4];
     *     if (copyIndexInto(index)) {
     *         // Effectuer l'interpolation
     *     }
     * }
     * </pre></blockquote>
     *
     * Le tableau {@code index} contiendra alors les valeurs <code>{1, 3, 5, 6}</code>.
     *
     * @param  index tableau dans lequel copier les champs {@code lower} et {@code upper}.
     * @return {@code false} s'il n'y a pas suffisament de données valides.
     */
    public abstract boolean copyIndexInto(int[] index);

    /**
     * Ajuste les index spécifiés de façon à ce qu'ils ne pointent vers aucune donnée {@link Double#NaN NaN}.
     * Ces index sont habituellement obtenus par la méthode {@link #copyIndexInto}.
     * <p>
     * Supposons que vous vous apprêtez à faire une interpolation polynomiale d'ordre 4 autour de la
     * donnée <var>x</var>=84. Supposons qu'avec les méthodes {@link #locate} et {@link #copyIndexInto},
     * vous avez obtenu les index {@code [4 5 6 7]}. La valeur 84 se trouvera typiquement entre
     * {@code x[5]} et {@code x[6]}. Maintenant supposons que votre vecteur des <var>y</var> contienne
     * les données suivantes:
     *
     * <blockquote><pre>
     * y = [5 3 1 2 7 NaN 12 6 4 ...etc...]
     * </pre></blockquote>
     *
     * Vous voulez vous assurez que les index obtenus par {@code copyIndexInto} pointent
     * tous vers une donnée <var>y</var> valide. Après avoir appellé la méthode
     *
     * <blockquote><pre>
     * validateIndex(y, index)
     * </pre></blockquote>
     *
     * vos index {@code [4 5 6 7]} deviendront {@code [3 4 6 7]}, car {@code y[5]} avait pour
     * valeur NaN. Notez que vous n'avez pas à vous préocupper de savoir si les index pointent
     * vers des <var>x</var> valides. Ça avait déjà été assuré par {@code copyIndexInto} et
     * continuera à être assuré par {@code validateIndex}.
     * <p>
     * Voici un exemple d'utilisation. Supposons que trois vecteurs de données ({@code Y1},
     * {@code Y2} et {@code Y3}) se partagent le même vecteur des <var>x</var> ({@code X}).
     * Supposons que vous souhaitez obtenir 4 index valides simultanément pour tous les vecteurs
     * autour de <var>x</var>=1045. Vous pourriez écrire:
     *
     * <blockquote><pre>
     * locate(1045);
     * final int index[] = new int[4];
     * copyIndexIntoArray(index);
     * boolean hasChanged;
     * do {
     *     hasChanged  = validateIndex(Y1, index);
     *     hasChanged |= validateIndex(Y2, index);
     *     hasChanged |= validateIndex(Y3, index);
     * } while (hasChanged);
     * </pre></blockquote>
     *
     * S'il n'est pas nécessaire que les index soient valides pour tous les vecteurs simultanément,
     * vous pourriez copier les éléments de {@code index} dans un tableau temporaire après l'appel
     * de {@code copyIndexInto}. Il vous suffira alors de restituer cette copie avant chaque appel
     * de {@code validateIndex} pour chacun des vecteurs {@code Y}. En réutilisant cette copie, vous
     * évitez d'appeller trois fois {@code locate} et y gagnez ainsi un peu en vitesse d'éxecution.
     *
     * @param y		Vecteur des données <var>y</var> servant à la vérification.
     * @param index A l'entrée, tableau d'index à vérifier. A la sortie, tableau d'index modifiés.
     *				Cette méthode s'efforce autant que possible de ne pas modifier les index se
     *				trouvant au centre de ce tableau.
     * @return		{@code true} si des changements ont été fait, {@code false} sinon.
     * @throws ExtrapolationException s'il n'y a pas suffisament de données valides.
     *
     * @see #locate(double)
     * @see #copyIndexInto(int[])
     */
    public final boolean validateIndex(final DoubleBuffer y, final int[] index) throws ExtrapolationException {
        assert isSorted(index) : Arrays.toString(index);
        boolean hasChanged = false;
        final int xlength = length();
        int center = index.length >> 1;
        loop: for (int i=center; --i>=0;) {
            if (isNaN(y.get(index[i]))) {
                /*
                 * Ce bloc ne sera exécuté que si un NaN a été trouvé (sinon cette méthode sera
                 * exécutée rapidement car elle n'aurait pratiquement rien à faire). La prochaine
                 * boucle décale les index qui avaient déjà été trouvés (par 'copyIndexInto') de
                 * façon à exclure les NaN. L'autre boucle va chercher d'autre index, de la même
                 * façon que 'copyIndexInto' s'y prenait.
                 */
                hasChanged = true;
                for (int j=i; --j>=0;) {
                    if (!isNaN(y.get(index[j]))) {
                        index[i--] = index[j];
                    }
                }
                int lower = index[0];
                do {
                    do if (--lower < 0) {
                        center -= ++i;
                        arraycopy(index, i, index, 0, index.length-i);
                        break loop;
                    } while (isNaN(get(lower)) || isNaN(y.get(lower)));
                    index[i--] = lower;
                } while (i >= 0);
                break loop;
            }
        }
        /*
         * Le code suivant fait la même opération que le code précédent,
         * mais pour la deuxième moitié des index.
         */
        loop: for (int i=center; i<index.length; i++) {
            if (isNaN(y.get(index[i]))) {
                hasChanged = true;
                for (int j=i; ++j<index.length;) {
                    if (!isNaN(y.get(index[j]))) {
                        index[i++] = index[j];
                    }
                }
                int upper = index[index.length-1];
                do {
                    do if (++upper >= xlength || upper >= y.limit()) {
                        int remainder = index.length-i;
                        // center += remainder; // (not needed)
                        arraycopy(index, 0, index, remainder, i);
                        i = remainder;
                        int lower = index[0];
                        do {
                            do if (--lower < 0) {
                                throw new ExtrapolationException();
                            } while (isNaN(get(lower)) || isNaN(y.get(lower)));
                            index[--i] = lower;
                        } while (i > 0);
                        break loop;
                    }
                    while (isNaN(get(upper)) || isNaN(y.get(upper)));
                    index[i++] = upper;
                } while (i < index.length);
                break loop;
            }
        }
        assert isSorted(index) : Arrays.toString(index);
        return hasChanged;
    }

    /**
     * Ajuste les index {@link #lower} et {@link #upper} de façon à ce qu'ils pointent vers des données
     * valides. Cette méthode est très similaire à la méthode {@link #validateIndex(DoubleBuffer,int[])},
     * excepté qu'elle agit directement sur {@link #lower} et {@link #upper} plutôt que sur un tableau
     * passé en argument. On y gagne ainsi en rapidité d'exécution (on évite de faire un appel à
     * {@link #copyIndexInto)}, mais ça ne gère toujours que ces deux index.
     * <p>
     * Tout ce qui était entre {@code lower} et {@code upper} avant l'appel de cette méthode le resteront
     * après. Cette méthode ne fait que diminuer {@code lower} et augmenter {@code upper}, si nécessaire.
     * Si ce n'était pas possible, une exception {@link ExtrapolationException} sera lancée.
     *
     * @param y Vecteur des données <var>y</var> servant à la vérification.
     * @return  {@code true} si des changements ont été fait, {@code false} sinon.
     * @throws  ExtrapolationException s'il n'y a pas suffisament de données valides.
     *
     * @see #validateIndex(DoubleBuffer[],int[])
     * @see #locateAroundIndex(int)
     * @see #locate(double)
     */
    public final boolean validateIndex(final DoubleBuffer y) throws ExtrapolationException {
        int[] check = null; // For assertions only. Next line has intentional side effect.
        assert isSorted(check=new int[] {lower, upper}) : Arrays.toString(check);

        boolean hasChanged = false;
        if (isNaN(y.get(upper))) {
            hasChanged = true;
            do if (++upper >= y.limit()) {
                throw new ExtrapolationException();
            } while (isNaN(get(upper)) || isNaN(y.get(upper)));
        }
        if (isNaN(y.get(lower))) {
            hasChanged = true;
            do if (--lower < 0) {
                throw new ExtrapolationException();
            } while (isNaN(get(lower)) || isNaN(y.get(lower)));
        }
        // Compare avec la version générique.
        assert hasChanged == validateIndex(y, check) : Arrays.toString(check);
        assert check[0] == lower : lower;
        assert check[1] == upper : upper;
        return hasChanged;
    }

    /**
     * Retourne une estimation de la plage de valeurs encadrées par {@link #lower} et {@link #upper}.
     * Cette plage est mesurée à partir de l'espace qui se trouve entre deux données, comme dans le
     * dessin ci-dessous:
     *
     * <blockquote><pre>
     *                      lower       upper
     *                        |           |
     * Indices: [ 0     1     2     3     4     5     6 ]
     * Valeurs: {146   148   150   152   154   156   158}
     *                     |                 |
     *                     ^------plage------^
     * </pre></blockquote>
     *
     * Dans l'exemple précédent, {@code getInterval()} retournerait la valeur 6. Notez que
     * {@code lower == upper} n'implique pas que cette méthode retourne 0. Dans l'exemple
     * précédent, {@code getInterval()} retournerait 2.
     * <p>
     * Il n'est pas obligatoire que l'intervalle entre les valeurs de ce vecteur soit constant.
     * Si ce vecteur contient des valeurs {@link Double#NaN NaN}, alors cette méthode utilisera
     * une interpolation linéaire.
     */
    public abstract double getInterval();

    /**
     * Retourne une copie de ce vecteur. La copie retournée devrait pouvoir être utilisée dans un
     * autre thread. Toutefois, les données sous-jacentes peuvent être partagées, de sorte que la
     * copie reste relativement économique.
     */
    @Override
    public OrderedVector clone() {
        try {
            return (OrderedVector) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen, since we are cloneable.
            throw new AssertionError(e);
        }
    }
}
