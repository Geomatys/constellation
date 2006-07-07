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

// J2SE and Java3D dependencies
import java.util.Locale;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.io.Writer;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.ReadOnlyBufferException;
import javax.vecmath.MismatchedSizeException;
import java.lang.reflect.Field;

// GeoAPI dependencies
import org.opengis.referencing.operation.Matrix;

// Static imports
import static java.lang.Math.abs;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.System.arraycopy;
import static org.geotools.resources.XArray.isSorted;
import static org.geotools.resources.Utilities.spaces;


/**
 * Classe de base pour les interpolations � une dimension. Cette classe m�morisera un vecteur des
 * <var>x</var> et un nombre arbitraire de vecteurs des <var>y</var> comme dans l'exemple ci-dessous:
 *
 * <blockquote><pre>
 * x<sub>1</sub>    y<sub>1</sub>    y'<sub>1</sub>    y"<sub>1</sub>
 * x<sub>2</sub>    y<sub>2</sub>    y'<sub>2</sub>    y"<sub>2</sub>
 * x<sub>3</sub>    y<sub>3</sub>    y'<sub>3</sub>    y"<sub>3</sub>
 * (...etc...)
 *	</pre></blockquote>
 *
 * Les donn�es du vecteur des <var>x</var> doivent obligatoirement �tre en ordre croissant ou
 * d�croissant (cette condition ne sera pas v�rifi�e, � la fois pour des raisons de performances
 * et parce que de toute fa�on on ne peut pas emp�cher l'utilisateur de changer ses donn�es apr�s
 * la cr�ation de la table). Celles des vecteur <var>y</var> peuvent �tre dans un ordre quelconque.
 * Tous les vecteurs doivent avoir la m�me longueur.
 * <p>
 * Les donn�es sont r�f�renc�es par des objets {@link DoubleBuffer}, ce qui permet entre autres de
 * r�f�rencer des sous-tableaux et d'utiliser les buffer hautes performances de {@link java.nio}.
 * Les donn�es elles-m�mes ne sont jamais copi�es, en aucun cas. On limite ainsi la consommation de
 * m�moire pour les gros tableaux.
 * <p>
 * Les donn�es manquantes (valeurs {@link Double#NaN NaN}) sont accept�es � la fois dans le vecteur
 * des <var>x</var> et dans les vecteurs des <var>y</var>. Par d�faut, les interpolations agissent
 * comme si elles utilisaient une copie des vecteurs des <var>x</var> et des <var>y</var> dans
 * lesquels toutes les valeurs {@link Double#NaN NaN} ont �t� omises (l'impl�mentation simule ce
 * comportement sans qu'aucune copie ne soit r�ellement effectu�e). Ce comportement peut �tre
 * d�sactiv� en appellant <code>{@linkplain #setSkipMissingY setSkipMissingY}(false)</code>.
 * <p>
 * Dans l'impl�mentation par d�faut de {@code Table}, la m�thode {@link #interpolate} renvoie la
 * valeur <var>y</var> du plus proche voisin. Les classes d�riv�es fourniront des interpolations
 * plus �labor�es (lin�aire, spline, <cite>etc.</cite>).
 * <p>
 * Les objets {@code Table} ne sont pas s�curitaire pour un environnement multi-threads. Si
 * plusieurs interpolations peuvent �tre fait simultan�ment par diff�rents threads, il faudra
 * une {@linkplain #clone copie} de {@code Table} pour chaque thread.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Table implements Matrix {
    /**
     * Nombre de chiffres significatifs � conserver lors de l'affichage du contenu d'une table.
     * Il ne s'agit pas forc�ment du nombre de chiffres apr�s la virgule. Il s'agit plut�t du
     * nombre de chiffres dans la partie des nombres ques l'on verra varier.
     */
    private static final int SIGNIFICANT_DIGIT = 4;

    /**
     * Nombre de chiffres � partir duquel on devrait utiliser la notation scientifique.
     */
    private static final int SCIENTIFIC_THRESHOLD = 5;

    /**
     * Vecteur des <var>x</var> de cette table. Ces donn�es doivent obligatoirement
     * �tre en ordre croissant ou d�croissant.
     */
    final OrderedVector x;

    /**
     * Vecteurs des <var>y</var> de cette table. Chacun de ces vecteurs doit avoir la m�me
     * longeur que {@link #x}.
     */
    private final DoubleBuffer[] y;

    /**
     * Indique aux m�thodes {@link #interpolate} si elles doivent ignorer les {@link Double#NaN NaN}
     * dans le vecteur des <var>y</var>. Cette valeur n'affecte pas le comportement de cette table
     * vis-�-vis le vecteur des <var>x</var>. En effet, les valeurs {@link Double#NaN NaN} dans ce
     * dernier sont toujours ignor�es.
     */
    private boolean skipMissingY;

    /**
     * Construit une table pour les vecteurs <var>x</var> et <var>y</var> sp�cifi�s. Les donn�es du
     * vecteur <var>x</var> doivent obligatoirement �tre en ordre croissant ou d�croissant.
     * <p>
     * Les premi�res donn�es prises en compte par la table seront les donn�es � la {@linkplain
     * DoubleBuffer#position() position courante} des vecteurs. Le nombre de donn�es pris en compte
     * sera le {@linkplain DoubleBuffer#remaining() nombre de donn�es restantes} dans chacun de ces
     * vecteurs. Apr�s la construction de la table, les changements de {@linkplain DoubleBuffer#position()
     * position} ou de {@linkplain DoubleBuffer#limit() limite} des objets {@code x} et {@code y} donn�s
     * en argument n'affecteront pas cette table. Toutefois tout changement des donn�es contenues dans
     * les buffers affecteront cette table.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement �tre en ordre croissant ou d�croissant.
     * @param y Les vecteurs des <var>y</var>.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la m�me longueur.
     */
    Table(final OrderedVector x, DoubleBuffer[] y) throws MismatchedSizeException {
        this.x = x;
        this.y = new DoubleBuffer[y.length];
        final int length = x.length();
        for (int i=0; i<y.length; i++) {
            if ((this.y[i] = y[i].slice()).remaining() != length) {
                throw new MismatchedSizeException();
            }
        }
        this.skipMissingY = true;
    }

    /**
     * Retourne le nombre de lignes dans cette table.
     */
    public final int getNumRow() {
        return x.length();
    }

    /**
     * Retourne le nombre de colonnes dans cette table. Il s'agit du nombre de vecteurs des
     * <var>y</var> plus 1 (le vecteur des <var>x</var>).
     */
    public final int getNumCol() {
        return y.length + 1;
    }

    /**
     * Modifie la valeur pour la ligne et colonne sp�cifi�es. La colonne 0 correspond au vecteur
     * des <var>x</var>. Toutes les autres colonnes correspondent aux vecteurs des <var>y</var>.
     * La plupart du temps, les valeurs de la colonne 0 ne sont pas modifiables.
     *
     * @param  row    La ligne d�sir�e, de 0 inclusivement jusqu'� {@link #getNumRow} exclusivement.
     * @param  column La colonne d�sir�e, de 0 inclusivement jusqu'� {@link #getNumCol} exclusivement.
     * @param  value  La nouvelle valeur � affecter � la position sp�cifi�e.
     * @throws IndexOutOfBoundsException si l'index de la ligne ou de la colonne est en dehors des
     *         limites permises.
     * @throws ReadOnlyBufferException si les valeurs de la colonne sp�cifi�e ne sont pas modifiables.
     */
    public final void setElement(final int row, final int column, final double value)
            throws IndexOutOfBoundsException, ReadOnlyBufferException
    {
        if (column == 0) {
            throw new ReadOnlyBufferException();
        }
        y[column-1].put(row, value);
    }

    /**
     * Retourne la valeur pour la ligne et colonne sp�cifi�es. La colonne 0 correspond au vecteur
     * des <var>x</var>. Toutes les autres colonnes correspondent aux vecteurs des <var>y</var>.
     *
     * @param  row    La ligne d�sir�e, de 0 inclusivement jusqu'� {@link #getNumRow} exclusivement.
     * @param  column La colonne d�sir�e, de 0 inclusivement jusqu'� {@link #getNumCol} exclusivement.
     * @return La valeur � la position sp�cifi�e.
     * @throws IndexOutOfBoundsException si l'index de la ligne ou de la colonne est en dehors des
     *         limites permises.
     */
    public final double getElement(final int row, final int column) throws IndexOutOfBoundsException {
        if (column == 0) {
            return x.get(row);
        } else {
            return y[column-1].get(row);
        }
    }

    /**
     * Renvoie la valeur <var>y<sub>i</sub></var> interpol�e � {@code x[row]} mais sans utiliser
     * la valeur de {@code y[row]}. Cette m�thode est utile pour boucher les trous caus�s par les
     * donn�es manquantes ({@link Double#NaN NaN}). On peut aussi utiliser cette m�thode pour
     * interpoler des pics isol�s qui semblent suspects. Toutefois s'il y a une possibilit� que
     * deux pics soient coll�s, il est pr�f�rable de remplacer tous les pics par des
     * {@link Double#NaN NaN} et ensuite d'utiliser cette m�thode pour combler les trous.
     *
     * @param  row index du <var>x</var> pour lequel on veut interpoler un <var>y</var>.
     * @param  column La colonne des <var>y</var>, habituellement � partir de 1 (car la
     *         colonne 0 est celle des <var>x</var>).
     * @throws IndexOutOfBoundsException si {@code column} n'est pas compris
     *         de 1 inclusivement � {@link #getNumCol} exclusivement.
     * @throws ExtrapolationException si une extrapolation non-permise a eu lieu.
     *
     * @see #getElement(int,int)
     * @see #interpolate(double,int)
     * @see #setSkipMissingY
     */
    public double interpolateAt(final int row, final int column) throws ExtrapolationException {
        if (!x.locateAroundIndex(row)) {
            throw new ExtrapolationException();
        }
        final DoubleBuffer y = this.y[column-1];
        if (skipMissingY) {
            x.validateIndex(y); // TODO: prendre en compte le cas o� d > 2
        }
        return interpolate(y);
    }

    /**
     * Renvoie la valeur <var>y<sub>i</sub></var> interpol�e au <var>x<sub>i</sub></var> sp�cifi� pour
     * la colonne des <var>y</var> sp�cifi�e. Cette m�thode est similaire � {@link #interpolate(double)}
     * pour une colonne arbitraire.
     *
     * @param  xi valeur de <var>x</var> pour laquelle on veut interpoler un <var>y</var>.
     * @param  column La colonne des <var>y</var>, habituellement � partir de 1 (car la
     *         colonne 0 est celle des <var>x</var>). Si cet argument est omis, alors la
     *         valeur par d�faut est 1.
     * @return valeur interpol�e.
     * @throws IndexOutOfBoundsException si {@code column} n'est pas compris
     *         de 1 inclusivement � {@link #getNumCol} exclusivement.
     * @throws ExtrapolationException si une extrapolation non-permise a eu lieu.
     *
     * @see #interpolate(double)
     * @see #setSkipMissingY
     */
    public final double interpolate(final double xi, final int column) throws ExtrapolationException {
        if (!x.locate(xi)) {
            throw new ExtrapolationException(xi);
        }
        final DoubleBuffer y = this.y[column-1];
        if (skipMissingY) {
            x.validateIndex(y); // TODO: prendre en compte le cas o� d > 2
        }
        return interpolate(y);
    }

    /**
     * Renvoie la valeur <var>y<sub>i</sub></var> interpol�e au <var>x<sub>i</sub></var> sp�cifi�.
     * Les valeurs {@link Double#NaN NaN} apparaissant dans le vecteur des <var>x</var> seront
     * toujours ignor�es. Celles qui apparaissent dans le vecteur des <var>y</var> seront ignor�es
     * aussi sauf si <code>{@linkplain #setSkipMissingY setSkipMissingY}(true)</code> a �t� appel�e.
     * <p>
     * Le type d'interpolation effectu�e d�pendra de la classe de cet objet. Par exemple la classe
     * {@link Spline} effectuera une interpolation cubique B-Spline. L'impl�mentation par d�faut
     * de {@code Table} retourne la valeur du plus proche voisin.
     *
     * @param  xi valeur de <var>x</var> pour laquelle on veut interpoler un <var>y</var>.
     * @return valeur interpol�e.
     * @throws ExtrapolationException si une extrapolation non-permise a eu lieu.
     *
     * @see #interpolate(double,int)
     * @see #setSkipMissingY
     */
    public final double interpolate(final double xi) throws ExtrapolationException {
        if (!x.locate(xi)) {
            throw new ExtrapolationException(xi);
        }
        final DoubleBuffer y = this.y[0];
        if (skipMissingY) {
            x.validateIndex(y); // TODO: prendre en compte le cas o� d > 2
        }
        return interpolate(y);
    }

    /**
     * Renvoie les valeurs <var>y<sub>i</sub></var> interpol�e au <var>x<sub>i</sub></var> sp�cifi�
     * pour toutes les colonnes des <var>y</var>. Les lignes utilis�es seront celles o� une valeur
     * valide (non {@link Double#NaN NaN}) existe pour <u>toutes</u> les colonnes des <var>y</var>
     * simultan�ment.
     *
     * @param  xi valeur de <var>x</var> pour laquelle on veut interpoler un <var>y</var>.
     * @param  dest Si non-null, tableau dans lequel enregistrer les r�sultats.
     * @return Les valeurs interpol�es dans le tableau {@code dest}, ou dans un nouveau tableau
     *         si {@code dest} �tait {@code null}.
     * @throws ExtrapolationException si une extrapolation non-permise a eu lieu.
     */
    public final double[] interpolateAll(final double xi, double[] dest) throws ExtrapolationException {
        if (!x.locate(xi)) {
            throw new ExtrapolationException(xi);
        }
        if (skipMissingY) {
            boolean hasChanged = false;
            do {
                for (int i=0; i<y.length; i++) {
                    hasChanged |= x.validateIndex(y[i]); // TODO: prendre en compte le cas o� d > 2
                }
            } while (hasChanged);
        }
        if (dest == null) {
            dest = new double[y.length];
        } else if (dest.length != y.length) {
            throw new IllegalArgumentException("La longueur du tableau de destination ne correspond pas.");
        }
        for (int i=0; i<y.length; i++) {
            dest[i] = interpolate(y[i]);
        }
        return dest;
    }

    /**
     * Renvoie la valeur <var>y<sub>i</sub></var> interpol�e � la position courante du vecteur des
     * <var>x</var>. La valeur n'est calcul�e que pour la colonne sp�cifi�e.
     * <p>
     * Le type d'interpolation effectu�e d�pendra de la classe de cet objet. Par exemple la classe
     * {@link Spline} effectuera une interpolation cubique B-Spline. L'impl�mentation par d�faut
     * de {@code Table} retourne la valeur du plus proche voisin.
     *
     * @param  y Le vecteur des <var>y</var> � utiliser.
     * @return La valeur interpol�e.
     */
    double interpolate(final DoubleBuffer y) {
        final double xi = x.value;
        final int lower = x.lower;
        final int upper = x.upper;
        return y.get((abs(x.get(lower)-xi) <= abs(x.get(upper)-xi)) ? lower : upper);        
    }

    /**
     * Trouve l'index de la valeur <var>x<sub>i</sub></var> dans le vecteur des <var>x</var> et
     * renvoie dans le tableau {@code index} les index qui y correspondent. Ce tableau peut avoir
     * une longueur quelconque. Cette m�thode tentera de cr�er une suite d'index, mais en sautant
     * les {@link Double#NaN NaN} qui apparaissent dans le vecteur des <var>x</var> ou le vecteur
     * des <var>y</var>. Par exemple supposons que cet objet repr�sente la table suivante:
     *
     * <blockquote><pre>
     *        [  0   1   2   3   4   5   6   7]
     *    X = [  2   4   5   7   8 NaN  12  14]
     *    Y = [  4   7   2   1   6   1 NaN   5]
     * </pre></blockquote>
     *
     * Alors, si {@code index} est un tableau de 4 �l�ments, {@code locate(10.0, index)} �crira
     * dans ce tableau les valeurs {@code [2 3 4 7]}. Les valeurs 5 et 6 ont �t� saut�es parce
     * que {@code X[5]==NaN} et {@code Y[6]==NaN}.
     *
     * @param xi valeur <var>x</var> dont on d�sire les index.
     * @param index tableau dans lequel �crire les index. Les valeurs de ce tableau seront �cras�es.
     */
    public final void locate(final double xi, final int[] index) throws ExtrapolationException {
        if (!x.locate(xi)) {
            throw new ExtrapolationException(xi);
        }
        assert x.get(x.lower) <= xi : x.lower;
        assert x.get(x.upper) >= xi : x.upper;
        final boolean fast = (index.length == 2);
        if (!fast) {
            x.copyIndexInto(index);
        }
        if (skipMissingY) {
            boolean hasChanged;
            do {
                hasChanged = false;
                for (int i=0; i<y.length; i++) {
                    hasChanged |= (fast ? x.validateIndex(y[i]) : x.validateIndex(y[i], index));
                }
            } while (hasChanged);
        }
        if (fast) {
            index[0] = x.lower;
            index[1] = x.upper;
            assert x.get(x.lower) <= xi : x.lower;
            assert x.get(x.upper) >= xi : x.upper;
        }
    }

    /**
     * Indique si les interpolations devront ignorer les valeurs {@link Double#NaN NaN} dans le
     * vecteur des <var>y</var>. La valeur {@code false} indique que les {@link Double#NaN NaN}
     * ne seront pas ignor�es, de sorte qu'ils peuvent �tre retourn�es par la m�thode
     * {@link #interpolate} s'ils apparaissent dans le vecteur des <var>y</var>. La valeurs
     * {@code true} indique au contraire que la m�thode {@link #interpolate} doit tenter de
     * n'utiliser que des valeurs r�elles (diff�rentes de {@link Double#NaN NaN}) pour ses
     * calculs.
     *
     * @param {@code true} si la m�thode {@link #interpolate} doit agir comme si elle utilisait des
     *        copies des donn�es dans lesquelles on avait retir� tout les {@link Double#NaN NaN}.
     */
    public void setSkipMissingY(final boolean skipMissingY) {
        this.skipMissingY = skipMissingY;
    }

    /**
     * Retourne presque toujours {@code false}.
     */
    public final boolean isIdentity() {
        final DoubleBuffer c;
        return x.length() == 2 && y.length == 1 &&
                 x.get(0) == 1 && x.get(1) == 0 &&
          (c=y[0]).get(0) == 0 && c.get(1) == 1;
    }

    /**
     * Ecrit toutes les valeurs de ce tableau vers le flot sp�cifi� en utilisant les conventions
     * locales.
     *
     * @param  out Le flot dans lequel �crire.
     * @throws IOException si une erreur est survenue lors de l'�criture.
     */
    public void print(final Writer out) throws IOException {
        print(out, Locale.getDefault(), 0, getNumRow());
    }

    /**
     * Ecrit les valeurs de ce tableau vers le flot sp�cifi�. Les lignes �crites iront de
     * {@code lower} inclusivement jusqu'� {@code upper} explusivement.
     *
     * @param  out    Le flot dans lequel �crire.
     * @param  locale Les conventions locales � utiliser pour l'�criture.
     * @param  lower  Num�ro de la premi�re ligne � �crire, inclusivement.
     * @param  upper  Num�ro de la derni�re ligne � �crire, exclusivement.
     * @throws IOException si une erreur est survenue lors de l'�criture.
     */
    public void print(final Writer out, final Locale locale,
                      final int lower, final int upper) throws IOException
    {
        final String   lineSeparator = System.getProperty("line.separator", "\n");
        final NumberFormat[] formats = new NumberFormat[getNumCol() + 1];
        final int[]          widths  = new int[formats.length];
        for (int i=0; i<formats.length; i++) {
            double min, max;
            if (i == 0) {
                min = lower;
                max = upper;
            } else {
                min = Double.POSITIVE_INFINITY;
                max = Double.NEGATIVE_INFINITY;
                for (int j=lower; j<upper; j++) {
                    final double v = getElement(j, i-1);
                    if (!Double.isInfinite(v)) {
                        if (v < min) min = v;
                        if (v > max) max = v;
                    }
                }
            }
            final NumberFormat format = NumberFormat.getInstance(locale);
            formats[i] = format;
            if (max > min) {
                int magnitude = (int) Math.floor(Math.log10(Math.max(Math.abs(min), Math.abs(max))));
                int precision = (i==0) ? 0 : (int) Math.ceil(SIGNIFICANT_DIGIT - Math.log10(max - min));
                int width     = Math.max(Math.max(magnitude, 0) + Math.max(precision, 0) + 2, 5);
                boolean   exp = (i!=0 && magnitude >= SCIENTIFIC_THRESHOLD);
                if (exp) {
                    if (format instanceof DecimalFormat) {
                        ((DecimalFormat) format).applyPattern("0.000E0");
                        precision -= magnitude;
                        width = precision + 4;
                    }
                }
                if (precision >= 0 && precision < 15) {
                    format.setMinimumFractionDigits(precision);
                    format.setMaximumFractionDigits(precision);
                }
                widths[i] = width;
            }
        }
        /*
         * Maintenant que les formats sont d�termin�s une fois pour toutes pour l'ensemble
         * des lignes et chaque colonne, proc�de � l'�criture.
         */
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition dummy = new FieldPosition(0);
        for (int j=lower; j<upper; j++) {
            for (int i=0; i<formats.length; i++) {
                buffer.setLength(0);
                final String value;
                if (i == 0) {
                    value = formats[i].format(j, buffer, dummy).toString();
                } else {
                    out.append('\t');
                    value = formats[i].format(getElement(j,i-1), buffer, dummy).toString();
                }
                out.append(spaces(widths[i] - value.length()));
                out.append(value);
            }
            out.append(lineSeparator);
        }
        out.flush();
    }

    /**
     * Retourne une courte repr�sentation textuelle de cette table. Cette cha�ne est utilis�e
     * surtout � des fins de d�boguage.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder("Table[");
        buffer.append(getNumRow());
        buffer.append(" rows, ");
        buffer.append(getNumCol());
        buffer.append(" columns, order=");
        buffer.append(x.getDataOrder().name());
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Retourne une copie de cette table. La copie partagera les m�mes donn�es que la table
     * originale. Cette copie est utile si des interpolations doivent �tre faites simultan�ment
     * dans diff�rents threads.
     */
    @Override
    public Table clone() {
        final Table copy;
        try {
            copy = (Table) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen, since we are cloneable.
            throw new AssertionError(e);
        }
        final Field x, y;
        try {
            x = Table.class.getField("x");
            y = Table.class.getField("y");
        } catch (NoSuchFieldException e) {
            // Should never happen, since thoses fields exist.
            throw new AssertionError(e);
        }
        x.setAccessible(true);
        y.setAccessible(true);
        try {
            x.set(copy, copy.x.clone());
            y.set(copy, copy.y.clone());
        } catch (IllegalAccessException e) {
            // Should never happen, since we made the field accessible.
            throw new AssertionError(e);
        }
        x.setAccessible(true);
        y.setAccessible(true);
        for (int i=0; i<copy.y.length; i++) {
            copy.y[i] = copy.y[i].duplicate();
        }
        return copy;
    }
}
