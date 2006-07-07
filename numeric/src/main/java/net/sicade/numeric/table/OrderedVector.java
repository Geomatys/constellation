/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 1997, P�ches et Oc�ans Canada
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.numeric.table;

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
import static org.geotools.resources.XArray.isSorted;


/**
 * Un vecteur de valeurs <var>x</var> dont toutes les donn�es sont en ordre croissant ou d�croissant.
 * Les valeurs {@link Double#NaN NaN} ne sont pas ordonn�es et peuvent appara�tre n'importe o� dans
 * ce vecteur. Notez que cette derni�re r�gle est diff�rente de celle de {@link Arrays#sort(double[])},
 * qui place les valeurs {@link Double#NaN NaN} � la fin du vecteur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class OrderedVector implements Cloneable {
	/**
     * Index des valeurs inf�rieure ({@code lower}) et sup�rieure ({@code upper}) � la valeur
     * sp�cifi�e lors du dernier appel de {@link #locate(double)}.
	 */
    protected int lower, upper;

    /**
     * Valeur pour laquelle l'utilisateur a demand� une interpolation. Cette valeur est m�moris�e
     * lors des appels � une m�thode {@code locate}. Si le dernier appel d'une telle m�thode a
     * retourn� {@code true} et que {@code value} n'est pas {@link Double#NaN NaN}, alors la
     * condition suivante doit �tre respect�e:
     *
     * <blockquote><pre>
     * {@linkplain #get get}({@linkplain #lower}) &lt= value &lt= {@linkplain #get get}({@linkplain #upper})
     * </pre></blockquote>
     */
    protected double value = Double.NaN;

    /**
     * Construit une nouvelle instance d'un vecteur ordonn�.
     */
    public OrderedVector() {
    }

    /**
     * Retourne l'ordre des donn�es dans ce vecteur. Les valeurs {@link Double#NaN NaN}
     * ne sont pas prises en compte pour d�terminer l'ordre.
     */
    public abstract DataOrder getDataOrder();

    /**
     * Retourne la longueur de ce vecteur.
     */
    public abstract int length();

    /**
     * Retourne la valeur � l'index sp�cifi�. La valeur de cet index peut varier de 0 inclusivement
     * jusqu'� {@link #length} exclusivement.
     * <p>
     * Cette m�thode peut �tre consid�r�e comme l'inverse de {@link #locate} dans la mesure o�
     * apr�s un appel � <code>{@linkplain #locate locate}({@linkplain #get get}(index))</code>,
     * on aura {@link #upper} == {@link #lower} == {@code index}.
     *
     * @param  index La valeur de l'index.
     * @return La valeur du vecteur � l'index sp�cifi�.
     * @throws IndexOutOfBoundsException si l'index est en dehors des limites permises.
     */
    public abstract double get(int index) throws IndexOutOfBoundsException;

    /**
     * Trouve les index {@link #lower} et {@link #upper} qui permettent d'encadrer la valeur
     * sp�cifi�e. Apr�s l'appel de cetet m�thode, les conditions suivantes sont respect�es:
     * <p>
     * <ul>
     *   <li><b>Si</b> une correspondance exacte est trouv�e entre la valeur <var>x</var>
     *       demand�e et une des valeurs de ce vecteur, <b>alors:</b>
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
     *               si les donn�es de ce vecteur sont en ordre croissant, ou</li>
     *           <li><code>{@linkplain #get get}({@linkplain #lower})</code> &gt; <var>x</var> &gt;
     *               <code>{@linkplain #get get}({@linkplain #upper})</code>
     *               si les donn�es de ce vecteur sont en ordre d�croissant.</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>
     * Mis � part les correspondances exactes, cette m�thode produira dans la plupart des cas un
     * r�sultat tel que <code>{@linkplain #upper} == {@linkplain #lower}+1</code>. Si toutefois
     * ce vecteur contient des valeurs {@link Double#NaN NaN}, alors l'�cart entre {@link #lower}
     * et {@link #upper} peut �tre plus grand, car cette m�thode s'efforce de faire pointer les
     * index {@link #lower} et {@link #upper} vers des valeurs r�elles.
     * <p>
     * <strong>Exemple:</strong>
     * Supposons que ce vecteur contient les donn�es suivantes:
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
     * Cette m�thode peut �tre consid�r�e comme l'inverse de {@link #get}.
     *
     * @param  x valeur � rechercher dans ce vecteur.
     * @return {@code true} si la valeur sp�cifi�e est comprise dans la plage de ce vecteur, ou
     *         {@code false} si elle se trouve en dehors ou si le vecteur n'a pas suffisamment de
     *         donn�es autres que {@link Double#NaN NaN}.
     */
    public abstract boolean locate(double x);

    /**
     * Positionne les index {@link #lower} et {@link #upper} autour de l'index sp�cifi�. Si ce
     * vecteur ne contient pas de valeurs {@link Double#NaN NaN}, alors {@link #lower} sera �gal
     * � {@code index-1} et {@link #upper} sera �gal � {@code index+1}. Si ce vecteur contient
     * des valeurs {@link Double#NaN NaN}, alors {@link #lower} peut �tre un peu plus bas et/ou
     * {@link #upper} un peu plus haut de fa�on � pointer sur des valeurs r�elles.
     *
     * @param  index Index de la valeur autour de laquelle on veut se positionner.
     * @return {@code true} si les bornes {@link #lower} et {@link #upper} ont �t� d�finie,
     *         ou {@code false} si l'index sp�cifi� tombe en dehors des limites du vecteur.
     */
    public abstract boolean locateAroundIndex(int index);

    /**
     * Copie dans le tableau sp�cifi� en argument la valeur des champs {@link #lower} et {@link #upper}.
     * Ce tableau peut avoir diverses longueurs. Un cas typique est lorsque qu'il a une longueur de 2.
     * Alors le champs {@link #lower} sera simplement copi� dans {@code index[0]} et {@link #upper}
     * dans {@code index[1]}. Si le tableau � une longueur de 1, alors seul {@link #lower} sera copi�
     * dans {@code index[0]}. Si le tableau � une longueur de 0, rien ne sera fait.
     * <p>
     * Les cas le plus int�ressants se produisent lorsque le tableau {@code index} � une longueur
     * de 3 et plus. Cette m�thode copiera les valeurs de {@link #lower} et {@link #upper} au milieu
     * de ce tableau, puis compl�tera les autres cellules avec la suite des index qui pointent vers
     * des valeurs autres que {@link Double#NaN NaN}. Par exemple si ce vecteur contient:
     *
     * <blockquote><pre>
     * Indices: [  0   1   2   3   4   5   6   7]
     * Valeurs: {  5   8 NaN  12 NaN  19  21  34}
     * </pre></blockquote>
     *
     * Alors l'appel de la m�thode <code>{@link #locate locate}(15)</code> donnera aux champs
     * {@link #lower} et {@link #upper} les valeurs 3 et 5 respectivement, de sorte que
     * <code>{@linkplain #get get}(3) &lt; 15 &lt; {@linkplain #get get}(5)</code>. Si vous souhaitez
     * effectuer une interpolation polynomiale d'ordre 4 autour de ces donn�es, vous pouvez �crire:
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
     * @return {@code false} s'il n'y a pas suffisament de donn�es valides.
     */
    public abstract boolean copyIndexInto(int[] index);

    /**
     * Ajuste les index sp�cifi�s de fa�on � ce qu'ils ne pointent vers aucune donn�e {@link Double#NaN NaN}.
     * Ces index sont habituellement obtenus par la m�thode {@link #copyIndexInto}.
     * <p>
     * Supposons que vous vous appr�tez � faire une interpolation polynomiale d'ordre 4 autour de la
     * donn�e <var>x</var>=84. Supposons qu'avec les m�thodes {@link #locate} et {@link #copyIndexInto},
     * vous avez obtenu les index {@code [4 5 6 7]}. La valeur 84 se trouvera typiquement entre
     * {@code x[5]} et {@code x[6]}. Maintenant supposons que votre vecteur des <var>y</var> contienne
     * les donn�es suivantes:
     *
     * <blockquote><pre>
     * y = [5 3 1 2 7 NaN 12 6 4 ...etc...]
     * </pre></blockquote>
     *
     * Vous voulez vous assurez que les index obtenus par {@code copyIndexInto} pointent
     * tous vers une donn�e <var>y</var> valide. Apr�s avoir appell� la m�thode
     *
     * <blockquote><pre>
     * validateIndex(y, index)
     * </pre></blockquote>
     *
     * vos index {@code [4 5 6 7]} deviendront {@code [3 4 6 7]}, car {@code y[5]} avait pour
     * valeur NaN. Notez que vous n'avez pas � vous pr�ocupper de savoir si les index pointent
     * vers des <var>x</var> valides. �a avait d�j� �t� assur� par {@code copyIndexInto} et
     * continuera � �tre assur� par {@code validateIndex}.
     * <p>
     * Voici un exemple d'utilisation. Supposons que trois vecteurs de donn�es ({@code Y1},
     * {@code Y2} et {@code Y3}) se partagent le m�me vecteur des <var>x</var> ({@code X}).
     * Supposons que vous souhaitez obtenir 4 index valides simultan�ment pour tous les vecteurs
     * autour de <var>x</var>=1045. Vous pourriez �crire:
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
     * S'il n'est pas n�cessaire que les index soient valides pour tous les vecteurs simultan�ment,
     * vous pourriez copier les �l�ments de {@code index} dans un tableau temporaire apr�s l'appel
     * de {@code copyIndexInto}. Il vous suffira alors de restituer cette copie avant chaque appel
     * de {@code validateIndex} pour chacun des vecteurs {@code Y}. En r�utilisant cette copie, vous
     * �vitez d'appeller trois fois {@code locate} et y gagnez ainsi un peu en vitesse d'�xecution.
     *
     * @param y		Vecteur des donn�es <var>y</var> servant � la v�rification.
     * @param index A l'entr�e, tableau d'index � v�rifier. A la sortie, tableau d'index modifi�s.
     *				Cette m�thode s'efforce autant que possible de ne pas modifier les index se
     *				trouvant au centre de ce tableau.
     * @return		{@code true} si des changements ont �t� fait, {@code false} sinon.
     * @throws ExtrapolationException s'il n'y a pas suffisament de donn�es valides.
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
                 * Ce bloc ne sera ex�cut� que si un NaN a �t� trouv� (sinon cette m�thode sera
                 * ex�cut�e rapidement car elle n'aurait pratiquement rien � faire). La prochaine
                 * boucle d�cale les index qui avaient d�j� �t� trouv�s (par 'copyIndexInto') de
                 * fa�on � exclure les NaN. L'autre boucle va chercher d'autre index, de la m�me
                 * fa�on que 'copyIndexInto' s'y prenait.
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
         * Le code suivant fait la m�me op�ration que le code pr�c�dent,
         * mais pour la deuxi�me moiti� des index.
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
     * Ajuste les index {@link #lower} et {@link #upper} de fa�on � ce qu'ils pointent vers des donn�es
     * valides. Cette m�thode est tr�s similaire � la m�thode {@link #validateIndex(DoubleBuffer,int[])},
     * except� qu'elle agit directement sur {@link #lower} et {@link #upper} plut�t que sur un tableau
     * pass� en argument. On y gagne ainsi en rapidit� d'ex�cution (on �vite de faire un appel �
     * {@link #copyIndexInto)}, mais �a ne g�re toujours que ces deux index.
     * <p>
     * Tout ce qui �tait entre {@code lower} et {@code upper} avant l'appel de cette m�thode le resteront
     * apr�s. Cette m�thode ne fait que diminuer {@code lower} et augmenter {@code upper}, si n�cessaire.
     * Si ce n'�tait pas possible, une exception {@link ExtrapolationException} sera lanc�e.
     *
     * @param y Vecteur des donn�es <var>y</var> servant � la v�rification.
     * @return  {@code true} si des changements ont �t� fait, {@code false} sinon.
     * @throws  ExtrapolationException s'il n'y a pas suffisament de donn�es valides.
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
        // Compare avec la version g�n�rique.
        assert hasChanged == validateIndex(y, check) : Arrays.toString(check);
        assert check[0] == lower : lower;
        assert check[1] == upper : upper;
        return hasChanged;
    }

    /**
     * Retourne une estimation de la plage de valeurs encadr�es par {@link #lower} et {@link #upper}.
     * Cette plage est mesur�e � partir de l'espace qui se trouve entre deux donn�es, comme dans le
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
     * Dans l'exemple pr�c�dent, {@code getInterval()} retournerait la valeur 6. Notez que
     * {@code lower == upper} n'implique pas que cette m�thode retourne 0. Dans l'exemple
     * pr�c�dent, {@code getInterval()} retournerait 2.
     * <p>
     * Il n'est pas obligatoire que l'intervalle entre les valeurs de ce vecteur soit constant.
     * Si ce vecteur contient des valeurs {@link Double#NaN NaN}, alors cette m�thode utilisera
     * une interpolation lin�aire.
     */
    public abstract double getInterval();

    /**
     * Retourne une copie de ce vecteur. La copie retourn�e devrait pouvoir �tre utilis�e dans un
     * autre thread. Toutefois, les donn�es sous-jacentes peuvent �tre partag�es, de sorte que la
     * copie reste relativement �conomique.
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
